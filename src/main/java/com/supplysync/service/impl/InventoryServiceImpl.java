package com.supplysync.service.impl;

import com.supplysync.dto.request.InventoryAdjustRequest;
import com.supplysync.dto.request.InventoryTransferRequest;
import com.supplysync.dto.response.InventorySnapshotResponse;
import com.supplysync.dto.response.InventoryTransactionResponse;
import com.supplysync.dto.response.InventoryTransferResponse;
import com.supplysync.dto.response.LowStockAlertResponse;
import com.supplysync.entity.Inventory;
import com.supplysync.entity.InventoryTransaction;
import com.supplysync.entity.Product;
import com.supplysync.entity.User;
import com.supplysync.entity.Warehouse;
import com.supplysync.enums.TransactionType;
import com.supplysync.exception.InsufficientInventoryException;
import com.supplysync.exception.InvalidOperationException;
import com.supplysync.exception.ResourceNotFoundException;
import com.supplysync.util.Constants;
import com.supplysync.kafka.producer.InventoryTransferEvent;
import com.supplysync.kafka.producer.InventoryUpdatedEvent;
import com.supplysync.kafka.producer.KafkaEventProducer;
import com.supplysync.mapper.EntityMapper;
import com.supplysync.repository.InventoryRepository;
import com.supplysync.repository.InventoryTransactionRepository;
import com.supplysync.repository.ProductRepository;
import com.supplysync.repository.UserRepository;
import com.supplysync.repository.WarehouseRepository;
import com.supplysync.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final KafkaEventProducer kafkaEventProducer;

    @Override
    @Transactional
    @CacheEvict(value = "inventory:low-stock", allEntries = true)
    public InventoryTransactionResponse adjustInventory(InventoryAdjustRequest request, Long performedByUserId) {
        log.info("Adjusting inventory - product: {}, warehouse: {}, type: {}, qty: {}",
                request.getProductId(), request.getWarehouseId(), request.getTransactionType(), request.getQuantity());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));

        User user = userRepository.findById(performedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + performedByUserId));

        if (request.getTransactionType() == TransactionType.TRANSFER) {
            throw new InvalidOperationException("INVALID_OPERATION", "Use the transfer endpoint to move stock between warehouses");
        }

        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId())
                .orElse(null);

        if (inventory == null) {
            inventory = Inventory.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .quantityAvailable(0)
                    .quantityReserved(0)
                    .quantityDamaged(0)
                    .build();
        }

        int qty = request.getQuantity();

        switch (request.getTransactionType()) {
            case INBOUND:
                inventory.setQuantityAvailable(inventory.getQuantityAvailable() + qty);
                break;
            case OUTBOUND:
                if (inventory.getQuantityAvailable() < qty) {
                    throw new InsufficientInventoryException(Constants.ERR_INSUFFICIENT_INVENTORY, "Insufficient inventory available");
                }
                inventory.setQuantityAvailable(inventory.getQuantityAvailable() - qty);
                break;
            case DAMAGE_REPORT:
                if (inventory.getQuantityAvailable() < qty) {
                    throw new InsufficientInventoryException(Constants.ERR_INSUFFICIENT_INVENTORY, "Insufficient inventory available");
                }
                inventory.setQuantityAvailable(inventory.getQuantityAvailable() - qty);
                inventory.setQuantityDamaged(inventory.getQuantityDamaged() + qty);
                break;
            case ADJUSTMENT:
                int newQty = inventory.getQuantityAvailable() + qty;
                if (newQty < 0) throw new InsufficientInventoryException(Constants.ERR_INSUFFICIENT_INVENTORY, "Insufficient inventory available");
                inventory.setQuantityAvailable(newQty);
                break;
            default:
                throw new InvalidOperationException("INVALID_OPERATION", "Invalid transaction type for adjustment");
        }

        inventory.setLastUpdatedAt(LocalDateTime.now());
        Inventory savedInventory = inventoryRepository.save(inventory);

        InventoryTransaction tx = InventoryTransaction.builder()
                .product(product)
                .warehouse(warehouse)
                .transactionType(request.getTransactionType())
                .quantity(qty)
                .performedBy(user)
                .notes(request.getNotes())
                .build();

        InventoryTransaction savedTx = transactionRepository.save(tx);

        // Publish event
        InventoryUpdatedEvent event = InventoryUpdatedEvent.builder()
                .productId(product.getId())
                .warehouseId(warehouse.getId())
                .transactionType(request.getTransactionType().name())
                .quantity(qty)
                .notes(request.getNotes())
                .performedByUserId(user.getId())
                .timestamp(LocalDateTime.now().toString())
                .build();
        kafkaEventProducer.publishInventoryUpdated(event);

        return EntityMapper.toTransactionResponse(savedTx);
    }

    @Override
    @Transactional
    @CacheEvict(value = "inventory:low-stock", allEntries = true)
    public InventoryTransferResponse transferInventory(InventoryTransferRequest request, Long performedByUserId) {
        log.info("Initiating inventory transfer: product {} from warehouse {} to warehouse {} qty {}",
                request.getProductId(), request.getSourceWarehouseId(), request.getDestinationWarehouseId(), request.getQuantity());

        if (request.getSourceWarehouseId().equals(request.getDestinationWarehouseId())) {
            throw new InvalidOperationException("INVALID_OPERATION", "Source and destination warehouses must be different");
        }

        User user = userRepository.findById(performedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + performedByUserId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        Warehouse srcWarehouse = warehouseRepository.findById(request.getSourceWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Source warehouse not found"));

        Warehouse destWarehouse = warehouseRepository.findById(request.getDestinationWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination warehouse not found"));

        // Pessimistic Lock on source inventory
        Inventory srcInv = inventoryRepository.findWithLockByProductIdAndWarehouseId(request.getProductId(), request.getSourceWarehouseId())
                .orElseThrow(() -> new InsufficientInventoryException(Constants.ERR_INSUFFICIENT_INVENTORY, "Insufficient inventory available"));

        if (srcInv.getQuantityAvailable() < request.getQuantity()) {
            throw new InsufficientInventoryException(Constants.ERR_INSUFFICIENT_INVENTORY, "Insufficient inventory available");
        }

        // Pessimistic Lock on destination inventory
        Inventory destInv = inventoryRepository.findWithLockByProductIdAndWarehouseId(request.getProductId(), request.getDestinationWarehouseId())
                .orElse(null);

        if (destInv == null) {
            destInv = Inventory.builder()
                    .product(product)
                    .warehouse(destWarehouse)
                    .quantityAvailable(0)
                    .quantityReserved(0)
                    .quantityDamaged(0)
                    .build();
        }

        // Deduct from source and add to destination
        srcInv.setQuantityAvailable(srcInv.getQuantityAvailable() - request.getQuantity());
        destInv.setQuantityAvailable(destInv.getQuantityAvailable() + request.getQuantity());

        srcInv.setLastUpdatedAt(LocalDateTime.now());
        destInv.setLastUpdatedAt(LocalDateTime.now());

        inventoryRepository.save(srcInv);
        inventoryRepository.save(destInv);

        // Create OUTBOUND transaction for source
        InventoryTransaction outTx = InventoryTransaction.builder()
                .product(product)
                .warehouse(srcWarehouse)
                .transactionType(TransactionType.OUTBOUND)
                .quantity(request.getQuantity())
                .referenceId("TRANSFER-SRC")
                .performedBy(user)
                .notes("Transfer to Warehouse " + destWarehouse.getWarehouseCode() + ". " + request.getNotes())
                .build();
        transactionRepository.save(outTx);

        // Create INBOUND transaction for destination
        InventoryTransaction inTx = InventoryTransaction.builder()
                .product(product)
                .warehouse(destWarehouse)
                .transactionType(TransactionType.INBOUND)
                .quantity(request.getQuantity())
                .referenceId("TRANSFER-DST")
                .performedBy(user)
                .notes("Transfer from Warehouse " + srcWarehouse.getWarehouseCode() + ". " + request.getNotes())
                .build();
        transactionRepository.save(inTx);

        // Publish event
        InventoryTransferEvent event = InventoryTransferEvent.builder()
                .productId(product.getId())
                .sourceWarehouseId(srcWarehouse.getId())
                .destinationWarehouseId(destWarehouse.getId())
                .quantity(request.getQuantity())
                .notes(request.getNotes())
                .performedByUserId(user.getId())
                .timestamp(LocalDateTime.now().toString())
                .build();
        kafkaEventProducer.publishInventoryTransferInitiated(event);

        // Publish source warehouse update event
        InventoryUpdatedEvent srcUpdateEvent = InventoryUpdatedEvent.builder()
                .productId(product.getId())
                .warehouseId(srcWarehouse.getId())
                .transactionType(TransactionType.TRANSFER.name())
                .quantity(request.getQuantity())
                .notes(request.getNotes())
                .performedByUserId(user.getId())
                .timestamp(LocalDateTime.now().toString())
                .build();
        kafkaEventProducer.publishInventoryUpdated(srcUpdateEvent);

        // Publish destination warehouse update event
        InventoryUpdatedEvent destUpdateEvent = InventoryUpdatedEvent.builder()
                .productId(product.getId())
                .warehouseId(destWarehouse.getId())
                .transactionType(TransactionType.TRANSFER.name())
                .quantity(request.getQuantity())
                .notes(request.getNotes())
                .performedByUserId(user.getId())
                .timestamp(LocalDateTime.now().toString())
                .build();
        kafkaEventProducer.publishInventoryUpdated(destUpdateEvent);

        return InventoryTransferResponse.builder()
                .productId(product.getId())
                .productSku(product.getSku())
                .productName(product.getName())
                .sourceWarehouseId(srcWarehouse.getId())
                .sourceWarehouseName(srcWarehouse.getName())
                .destinationWarehouseId(destWarehouse.getId())
                .destinationWarehouseName(destWarehouse.getName())
                .quantity(request.getQuantity())
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "inventory:low-stock", key = "''")
    public List<LowStockAlertResponse> getLowStockAlerts() {
        log.info("Fetching low stock alerts from database");
        List<Inventory> lowStockItems = inventoryRepository.findLowStockInventory();
        return lowStockItems.stream()
                .map(inv -> LowStockAlertResponse.builder()
                        .productId(inv.getProduct().getId())
                        .sku(inv.getProduct().getSku())
                        .productName(inv.getProduct().getName())
                        .warehouseId(inv.getWarehouse().getId())
                        .warehouseName(inv.getWarehouse().getName())
                        .quantityAvailable(inv.getQuantityAvailable())
                        .reorderLevel(inv.getProduct().getReorderLevel())
                        .deficit(inv.getProduct().getReorderLevel() - inv.getQuantityAvailable())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventorySnapshotResponse> getWarehouseInventory(Long warehouseId, Pageable pageable) {
        log.info("Fetching inventory snapshot for warehouse ID: {}", warehouseId);
        Page<Inventory> inventoryPage = inventoryRepository.findByWarehouseId(warehouseId, pageable);
        return inventoryPage.map(EntityMapper::toSnapshotResponse);
    }
}
