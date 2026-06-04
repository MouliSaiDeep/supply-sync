package com.supplysync.service.impl;

import com.supplysync.dto.request.InventoryAdjustRequest;
import com.supplysync.dto.request.PurchaseOrderReceiptItemRequest;
import com.supplysync.dto.request.PurchaseOrderReceiptRequest;
import com.supplysync.dto.request.PurchaseOrderRequest;
import com.supplysync.dto.response.PurchaseOrderResponse;
import com.supplysync.entity.Product;
import com.supplysync.entity.PurchaseOrder;
import com.supplysync.entity.PurchaseOrderItem;
import com.supplysync.entity.Supplier;
import com.supplysync.entity.User;
import com.supplysync.entity.Warehouse;
import com.supplysync.enums.PurchaseOrderStatus;
import com.supplysync.enums.TransactionType;
import org.springframework.security.access.AccessDeniedException;
import com.supplysync.exception.InvalidOperationException;
import com.supplysync.exception.ResourceNotFoundException;
import com.supplysync.util.Constants;
import com.supplysync.kafka.producer.KafkaEventProducer;
import com.supplysync.kafka.producer.PurchaseOrderReceivedEvent;
import com.supplysync.mapper.EntityMapper;
import com.supplysync.repository.ProductRepository;
import com.supplysync.repository.PurchaseOrderItemRepository;
import com.supplysync.repository.PurchaseOrderRepository;
import com.supplysync.repository.SupplierRepository;
import com.supplysync.repository.UserRepository;
import com.supplysync.repository.WarehouseRepository;
import com.supplysync.service.InventoryService;
import com.supplysync.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final StringRedisTemplate redisTemplate;
    private final KafkaEventProducer kafkaEventProducer;

    private User getCurrentUser() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        throw new ResourceNotFoundException("No authenticated user found in security context");
    }

    private String generatePoNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "po-sequence:" + dateStr;
        long seq = 1;
        try {
            Long incremented = redisTemplate.opsForValue().increment(key);
            if (incremented != null) {
                seq = incremented;
                if (seq == 1) {
                    redisTemplate.expire(key, 24, TimeUnit.HOURS);
                }
            }
        } catch (Exception e) {
            log.warn("Redis not available for daily PO sequence generation, falling back to random: {}", e.getMessage());
            seq = new Random().nextInt(9000) + 1000;
        }
        seq = Math.min(seq, 9999L);
        return "PO-" + dateStr + "-" + String.format("%04d", seq);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        log.info("Creating purchase order in DRAFT status");
        User creator = getCurrentUser();

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + request.getSupplierId()));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));

        String poNumber = generatePoNumber();

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(poNumber)
                .supplier(supplier)
                .warehouse(warehouse)
                .status(PurchaseOrderStatus.DRAFT)
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .createdBy(creator)
                .notes(request.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        PurchaseOrder savedPo = purchaseOrderRepository.save(po);

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseOrderItem> items = new ArrayList<>();

        for (var itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));

            BigDecimal itemTotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantityOrdered()));
            total = total.add(itemTotal);

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(savedPo)
                    .product(product)
                    .quantityOrdered(itemReq.getQuantityOrdered())
                    .quantityReceived(0)
                    .unitPrice(itemReq.getUnitPrice())
                    .totalPrice(itemTotal)
                    .build();

            items.add(purchaseOrderItemRepository.save(item));
        }

        savedPo.setItems(items);
        savedPo.setTotalAmount(total);
        PurchaseOrder finalPo = purchaseOrderRepository.save(savedPo);

        log.info("Purchase order created successfully: {}", finalPo.getPoNumber());
        return EntityMapper.toResponse(finalPo);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse submitPurchaseOrder(Long id) {
        log.info("Submitting purchase order ID: {} for approval", id);
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));

        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new InvalidOperationException("INVALID_OPERATION", "Purchase order must be in DRAFT status to submit");
        }

        if (po.getItems() == null || po.getItems().isEmpty()) {
            throw new InvalidOperationException("INVALID_OPERATION", "Purchase order must have at least one line item");
        }

        po.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return EntityMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse approvePurchaseOrder(Long id, Long approvedByUserId) {
        log.info("Approving purchase order ID: {}", id);
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));

        if (po.getStatus() != PurchaseOrderStatus.PENDING_APPROVAL) {
            throw new InvalidOperationException("INVALID_OPERATION", "Purchase order is not in PENDING_APPROVAL status");
        }

        if (po.getCreatedBy().getId().equals(approvedByUserId)) {
            log.warn("Self-approval attempt blocked for PO: {}", po.getPoNumber());
            throw new AccessDeniedException("SELF_APPROVAL_NOT_ALLOWED");
        }

        User approver = userRepository.findById(approvedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found: " + approvedByUserId));

        po.setStatus(PurchaseOrderStatus.APPROVED);
        po.setApprovedBy(approver);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        log.info("Purchase order approved successfully: {}", saved.getPoNumber());
        return EntityMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse receivePurchaseOrder(Long id, PurchaseOrderReceiptRequest request, Long performedByUserId) {
        log.info("Receiving items for purchase order ID: {}", id);
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));

        if (po.getStatus() != PurchaseOrderStatus.APPROVED &&
                po.getStatus() != PurchaseOrderStatus.ORDERED &&
                po.getStatus() != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new InvalidOperationException("INVALID_OPERATION", "Purchase order status must be APPROVED, ORDERED, or PARTIALLY_RECEIVED to receive items");
        }

        for (PurchaseOrderReceiptItemRequest receiptItem : request.getItems()) {
            PurchaseOrderItem item = po.getItems().stream()
                    .filter(i -> i.getId().equals(receiptItem.getPoItemId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("PO line item not found with ID: " + receiptItem.getPoItemId()));

            if (item.getQuantityReceived() + receiptItem.getQuantityReceived() > item.getQuantityOrdered()) {
                throw new InvalidOperationException("INVALID_OPERATION", "Received quantity exceeds quantity ordered for item ID: " + item.getId());
            }

            item.setQuantityReceived(item.getQuantityReceived() + receiptItem.getQuantityReceived());
            purchaseOrderItemRepository.save(item);

            // Trigger INBOUND inventory adjustment
            InventoryAdjustRequest adjustRequest = new InventoryAdjustRequest();
            adjustRequest.setProductId(item.getProduct().getId());
            adjustRequest.setWarehouseId(po.getWarehouse().getId());
            adjustRequest.setTransactionType(TransactionType.INBOUND);
            adjustRequest.setQuantity(receiptItem.getQuantityReceived());
            adjustRequest.setNotes("Received from PO: " + po.getPoNumber());
            inventoryService.adjustInventory(adjustRequest, performedByUserId);
        }

        // Evaluate overall received status
        boolean allFullyReceived = po.getItems().stream()
                .allMatch(i -> i.getQuantityReceived().equals(i.getQuantityOrdered()));

        if (allFullyReceived) {
            po.setStatus(PurchaseOrderStatus.RECEIVED);
            po.setActualDeliveryDate(request.getActualDeliveryDate());
        } else {
            po.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }

        PurchaseOrder saved = purchaseOrderRepository.save(po);

        // Publish event
        PurchaseOrderReceivedEvent event = PurchaseOrderReceivedEvent.builder()
                .poId(po.getId())
                .poNumber(po.getPoNumber())
                .actualDeliveryDate(request.getActualDeliveryDate().toString())
                .timestamp(LocalDateTime.now().toString())
                .build();
        kafkaEventProducer.publishPurchaseOrderReceived(event);

        log.info("Purchase order receipt logged successfully. Status: {}", saved.getStatus());
        return EntityMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse cancelPurchaseOrder(Long id, String reason) {
        log.info("Cancelling purchase order ID: {} with reason: {}", id, reason);
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));

        if (po.getStatus() == PurchaseOrderStatus.RECEIVED ||
                po.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED ||
                po.getStatus() == PurchaseOrderStatus.ORDERED) {
            throw new InvalidOperationException(Constants.ERR_PO_CANCELLATION, "Purchase order cannot be cancelled in its current status");
        }

        po.setStatus(PurchaseOrderStatus.CANCELLED);
        po.setNotes((po.getNotes() != null ? po.getNotes() + "\n" : "") + "Cancelled: " + reason);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        log.info("Purchase order cancelled: {}", saved.getPoNumber());
        return EntityMapper.toResponse(saved);
    }
}
