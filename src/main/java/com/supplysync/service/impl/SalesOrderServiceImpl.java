package com.supplysync.service.impl;

import com.supplysync.dto.request.SalesOrderItemRequest;
import com.supplysync.dto.request.SalesOrderRequest;
import com.supplysync.dto.response.SalesOrderResponse;
import com.supplysync.entity.Inventory;
import com.supplysync.entity.InventoryTransaction;
import com.supplysync.entity.Product;
import com.supplysync.entity.SalesOrder;
import com.supplysync.entity.SalesOrderItem;
import com.supplysync.entity.User;
import com.supplysync.entity.Warehouse;
import com.supplysync.enums.SalesOrderStatus;
import com.supplysync.enums.TransactionType;
import com.supplysync.exception.InsufficientInventoryException;
import com.supplysync.exception.InvalidOperationException;
import com.supplysync.exception.ResourceNotFoundException;
import com.supplysync.util.Constants;
import com.supplysync.kafka.producer.KafkaEventProducer;
import com.supplysync.kafka.producer.SalesOrderCancelledEvent;
import com.supplysync.kafka.producer.SalesOrderCreatedEvent;
import com.supplysync.kafka.producer.InventoryUpdatedEvent;
import com.supplysync.mapper.EntityMapper;
import com.supplysync.repository.InventoryRepository;
import com.supplysync.repository.InventoryTransactionRepository;
import com.supplysync.repository.ProductRepository;
import com.supplysync.repository.SalesOrderItemRepository;
import com.supplysync.repository.SalesOrderRepository;
import com.supplysync.repository.UserRepository;
import com.supplysync.repository.WarehouseRepository;
import com.supplysync.service.SalesOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final KafkaEventProducer kafkaEventProducer;

    private User getCurrentUser() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        throw new ResourceNotFoundException("No authenticated user found in security context");
    }

    @Override
    @Transactional
    @CacheEvict(value = "inventory:low-stock", allEntries = true)
    public SalesOrderResponse createSalesOrder(SalesOrderRequest request, Long createdByUserId) {
        log.info("Creating sales order for customer: {}", request.getCustomerName());

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));

        User creator = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found with ID: " + createdByUserId));

        List<String> shortItems = new ArrayList<>();

        // Validate stock availability for all items
        for (SalesOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemReq.getProductId()));

            Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(itemReq.getProductId(), request.getWarehouseId())
                    .orElse(null);

            int available = (inventory != null) ? inventory.getQuantityAvailable() : 0;
            if (available < itemReq.getQuantity()) {
                shortItems.add("Product SKU " + product.getSku() + " is short by " + (itemReq.getQuantity() - available) + " units");
            }
        }

        if (!shortItems.isEmpty()) {
            log.warn("Insufficient stock for sales order creation: {}", shortItems);
            throw new InsufficientInventoryException(Constants.ERR_INSUFFICIENT_STOCK, "Insufficient stock for sales order creation", shortItems);
        }

        // Reserve stock
        for (SalesOrderItemRequest itemReq : request.getItems()) {
            Inventory inventory = inventoryRepository.findWithLockByProductIdAndWarehouseId(itemReq.getProductId(), request.getWarehouseId())
                    .or(() -> inventoryRepository.findByProductIdAndWarehouseId(itemReq.getProductId(), request.getWarehouseId()))
                    .orElseThrow(() -> new InsufficientInventoryException(Constants.ERR_INSUFFICIENT_STOCK, "Insufficient stock for sales order creation"));

            inventory.setQuantityAvailable(inventory.getQuantityAvailable() - itemReq.getQuantity());
            inventory.setQuantityReserved(inventory.getQuantityReserved() + itemReq.getQuantity());
            inventory.setLastUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);
        }

        // Generate unique order number
        String orderNumber = "SO-" + RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        while (salesOrderRepository.existsByOrderNumber(orderNumber)) {
            orderNumber = "SO-" + RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        }

        SalesOrder so = SalesOrder.builder()
                .orderNumber(orderNumber)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .shippingAddress(request.getShippingAddress())
                .warehouse(warehouse)
                .status(SalesOrderStatus.CONFIRMED)
                .createdBy(creator)
                .notes(request.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        SalesOrder savedSo = salesOrderRepository.save(so);

        BigDecimal total = BigDecimal.ZERO;
        List<SalesOrderItem> items = new ArrayList<>();

        for (SalesOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId()).get();
            BigDecimal itemTotal = product.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(itemTotal);

            SalesOrderItem item = SalesOrderItem.builder()
                    .salesOrder(savedSo)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getUnitPrice())
                    .totalPrice(itemTotal)
                    .build();

            items.add(salesOrderItemRepository.save(item));
        }

        savedSo.setItems(items);
        savedSo.setTotalAmount(total);
        SalesOrder finalSo = salesOrderRepository.save(savedSo);

        // Publish event
        SalesOrderCreatedEvent event = SalesOrderCreatedEvent.builder()
                .salesOrderId(finalSo.getId())
                .orderNumber(finalSo.getOrderNumber())
                .warehouseId(warehouse.getId())
                .totalAmount(total)
                .timestamp(LocalDateTime.now().toString())
                .build();
        kafkaEventProducer.publishSalesOrderCreated(event);

        log.info("Sales order created successfully in CONFIRMED status: {}", finalSo.getOrderNumber());
        return EntityMapper.toResponse(finalSo);
    }

    @Override
    @Transactional
    public SalesOrderResponse dispatchSalesOrder(Long id) {
        User user = getCurrentUser();
        return dispatchSalesOrder(id, user.getId());
    }

    @Override
    @Transactional
    public SalesOrderResponse dispatchSalesOrder(Long id, Long performedByUserId) {
        log.info("Dispatching sales order ID: {}", id);
        SalesOrder so = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with ID: " + id));

        if (so.getStatus() != SalesOrderStatus.CONFIRMED && so.getStatus() != SalesOrderStatus.PROCESSING) {
            throw new InvalidOperationException("INVALID_OPERATION", "Sales order must be CONFIRMED or PROCESSING to dispatch");
        }

        User dispatcher = userRepository.findById(performedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + performedByUserId));

        // Release reservations and create OUTBOUND transactions
        for (SalesOrderItem item : so.getItems()) {
            Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(
                    item.getProduct().getId(), so.getWarehouse().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

            if (inventory.getQuantityReserved() < item.getQuantity()) {
                throw new InvalidOperationException("INVALID_OPERATION", "Reserved quantity is less than item quantity for item ID: " + item.getId());
            }

            inventory.setQuantityReserved(inventory.getQuantityReserved() - item.getQuantity());
            inventory.setLastUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);

            // Log OUTBOUND transaction
            InventoryTransaction tx = InventoryTransaction.builder()
                    .product(item.getProduct())
                    .warehouse(so.getWarehouse())
                    .transactionType(TransactionType.OUTBOUND)
                    .quantity(item.getQuantity())
                    .referenceId("SO-DISPATCH:" + so.getOrderNumber())
                    .performedBy(dispatcher)
                    .notes("Dispatched for Sales Order: " + so.getOrderNumber())
                    .build();
            transactionRepository.save(tx);

            InventoryUpdatedEvent invEvent = InventoryUpdatedEvent.builder()
                .productId(item.getProduct().getId())
                .warehouseId(so.getWarehouse().getId())
                .transactionType(TransactionType.OUTBOUND.name())
                .quantity(item.getQuantity())
                .notes("Dispatched for Sales Order: " + so.getOrderNumber())
                .performedByUserId(performedByUserId)
                .timestamp(LocalDateTime.now().toString())
                .build();
            kafkaEventProducer.publishInventoryUpdated(invEvent);
        }

        so.setStatus(SalesOrderStatus.DISPATCHED);
        so.setDispatchedAt(LocalDateTime.now());
        SalesOrder saved = salesOrderRepository.save(so);

        log.info("Sales order dispatched successfully: {}", saved.getOrderNumber());
        return EntityMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SalesOrderResponse deliverSalesOrder(Long id) {
        log.info("Delivering sales order ID: {}", id);
        SalesOrder so = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with ID: " + id));

        if (so.getStatus() != SalesOrderStatus.DISPATCHED) {
            throw new InvalidOperationException("INVALID_OPERATION", "Sales order must be DISPATCHED before delivering");
        }

        so.setStatus(SalesOrderStatus.DELIVERED);
        so.setDeliveredAt(LocalDateTime.now());
        SalesOrder saved = salesOrderRepository.save(so);

        log.info("Sales order delivered successfully: {}", saved.getOrderNumber());
        return EntityMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "inventory:low-stock", allEntries = true)
    public SalesOrderResponse cancelSalesOrder(Long id, String reason) {
        log.info("Cancelling sales order ID: {} for reason: {}", id, reason);
        SalesOrder so = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with ID: " + id));

        if (so.getStatus() != SalesOrderStatus.PENDING && so.getStatus() != SalesOrderStatus.CONFIRMED) {
            log.warn("Cannot cancel sales order {} because it is in status {}", id, so.getStatus());
            throw new InvalidOperationException("INVALID_OPERATION", "Order cannot be cancelled. Already dispatched/delivered.");
        }

        // Release reservations
        for (SalesOrderItem item : so.getItems()) {
            Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(
                    item.getProduct().getId(), so.getWarehouse().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

            inventory.setQuantityAvailable(inventory.getQuantityAvailable() + item.getQuantity());
            inventory.setQuantityReserved(inventory.getQuantityReserved() - item.getQuantity());
            inventory.setLastUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);
        }

        so.setStatus(SalesOrderStatus.CANCELLED);
        so.setNotes((so.getNotes() != null ? so.getNotes() + "\n" : "") + "Cancelled: " + reason);
        SalesOrder saved = salesOrderRepository.save(so);

        // Publish event
        SalesOrderCancelledEvent event = SalesOrderCancelledEvent.builder()
                .salesOrderId(saved.getId())
                .orderNumber(saved.getOrderNumber())
                .reason(reason)
                .timestamp(LocalDateTime.now().toString())
                .build();
        kafkaEventProducer.publishSalesOrderCancelled(event);

        log.info("Sales order cancelled: {}", saved.getOrderNumber());
        return EntityMapper.toResponse(saved);
    }
}
