package com.supplysync.service;

import com.supplysync.dto.request.InventoryAdjustRequest;
import com.supplysync.dto.request.InventoryTransferRequest;
import com.supplysync.dto.response.InventoryTransactionResponse;
import com.supplysync.dto.response.InventoryTransferResponse;
import com.supplysync.dto.response.LowStockAlertResponse;
import com.supplysync.entity.*;
import com.supplysync.enums.TransactionType;
import com.supplysync.exception.InsufficientInventoryException;
import com.supplysync.kafka.producer.KafkaEventProducer;
import com.supplysync.repository.*;
import com.supplysync.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryTransactionRepository transactionRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private KafkaEventProducer kafkaEventProducer;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product product;
    private Warehouse warehouse;
    private Warehouse destWarehouse;
    private User user;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(1L).sku("SKU-123").name("Test Product").reorderLevel(10).unitPrice(BigDecimal.TEN).build();
        warehouse = Warehouse.builder().id(1L).warehouseCode("WH-123").name("Warehouse 1").build();
        destWarehouse = Warehouse.builder().id(2L).warehouseCode("WH-456").name("Warehouse 2").build();
        user = User.builder().id(1L).username("testuser").build();
        inventory = Inventory.builder().id(1L).product(product).warehouse(warehouse).quantityAvailable(50).quantityReserved(0).quantityDamaged(0).build();
    }

    @Test
    void adjustInventory_shouldCreateTransactionRecord_whenInboundAdjustment() {
        InventoryAdjustRequest request = new InventoryAdjustRequest();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setTransactionType(TransactionType.INBOUND);
        request.setQuantity(20);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryTransaction tx = InventoryTransaction.builder().id(1L).product(product).warehouse(warehouse).transactionType(TransactionType.INBOUND).quantity(20).performedBy(user).build();
        when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(tx);

        InventoryTransactionResponse response = inventoryService.adjustInventory(request, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(transactionRepository, times(1)).save(any(InventoryTransaction.class));
    }

    @Test
    void adjustInventory_shouldThrowInsufficientInventoryException_whenOutboundExceedsAvailable() {
        InventoryAdjustRequest request = new InventoryAdjustRequest();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setTransactionType(TransactionType.OUTBOUND);
        request.setQuantity(100); // Exceeds 50

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientInventoryException.class, () -> inventoryService.adjustInventory(request, 1L));
        verify(transactionRepository, never()).save(any(InventoryTransaction.class));
    }

    @Test
    void adjustInventory_shouldPublishKafkaEvent_onSuccessfulAdjustment() {
        InventoryAdjustRequest request = new InventoryAdjustRequest();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setTransactionType(TransactionType.INBOUND);
        request.setQuantity(20);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        InventoryTransaction tx = InventoryTransaction.builder().id(1L).product(product).warehouse(warehouse).transactionType(TransactionType.INBOUND).quantity(20).performedBy(user).build();
        when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(tx);

        inventoryService.adjustInventory(request, 1L);

        verify(kafkaEventProducer, times(1)).publishInventoryUpdated(any());
    }

    @Test
    void transferInventory_shouldDeductFromSourceAndAddToDestination_atomically() {
        InventoryTransferRequest request = new InventoryTransferRequest();
        request.setProductId(1L);
        request.setSourceWarehouseId(1L);
        request.setDestinationWarehouseId(2L);
        request.setQuantity(20);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destWarehouse));
        when(inventoryRepository.findWithLockByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));

        Inventory destInv = Inventory.builder().id(2L).product(product).warehouse(destWarehouse).quantityAvailable(5).quantityReserved(0).quantityDamaged(0).build();
        when(inventoryRepository.findWithLockByProductIdAndWarehouseId(1L, 2L)).thenReturn(Optional.of(destInv));

        InventoryTransferResponse response = inventoryService.transferInventory(request, 1L);

        assertNotNull(response);
        assertEquals(30, inventory.getQuantityAvailable()); // 50 - 20
        assertEquals(25, destInv.getQuantityAvailable()); // 5 + 20
        verify(inventoryRepository, times(2)).save(any(Inventory.class));
        verify(transactionRepository, times(2)).save(any(InventoryTransaction.class));
    }

    @Test
    void transferInventory_shouldThrowException_whenSourceHasInsufficientStock() {
        InventoryTransferRequest request = new InventoryTransferRequest();
        request.setProductId(1L);
        request.setSourceWarehouseId(1L);
        request.setDestinationWarehouseId(2L);
        request.setQuantity(100); // Exceeds 50

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destWarehouse));
        when(inventoryRepository.findWithLockByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientInventoryException.class, () -> inventoryService.transferInventory(request, 1L));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void getLowStockAlerts_shouldReturnProducts_belowReorderLevel() {
        inventory.setQuantityAvailable(5); // Below reorder level 10
        when(inventoryRepository.findLowStockInventory()).thenReturn(List.of(inventory));

        List<LowStockAlertResponse> alerts = inventoryService.getLowStockAlerts();

        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        assertEquals(5, alerts.get(0).getDeficit()); // 10 - 5
    }
}
