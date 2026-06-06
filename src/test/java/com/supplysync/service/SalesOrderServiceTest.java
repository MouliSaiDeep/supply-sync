package com.supplysync.service;

import com.supplysync.dto.request.SalesOrderItemRequest;
import com.supplysync.dto.request.SalesOrderRequest;
import com.supplysync.dto.response.SalesOrderResponse;
import com.supplysync.entity.*;
import com.supplysync.enums.SalesOrderStatus;
import com.supplysync.exception.InsufficientInventoryException;
import com.supplysync.exception.InvalidOperationException;
import com.supplysync.kafka.producer.KafkaEventProducer;
import com.supplysync.repository.*;
import com.supplysync.service.impl.SalesOrderServiceImpl;
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
public class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private SalesOrderItemRepository salesOrderItemRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryTransactionRepository transactionRepository;
    @Mock
    private KafkaEventProducer kafkaEventProducer;

    @InjectMocks
    private SalesOrderServiceImpl salesOrderService;

    private Product product;
    private Warehouse warehouse;
    private User user;
    private Inventory inventory;
    private SalesOrder salesOrder;
    private SalesOrderItem salesOrderItem;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(1L).sku("SKU-123").name("Test Product").unitPrice(BigDecimal.TEN).build();
        warehouse = Warehouse.builder().id(1L).warehouseCode("WH-123").name("Warehouse 1").build();
        user = User.builder().id(1L).username("testuser").build();
        inventory = Inventory.builder().id(1L).product(product).warehouse(warehouse).quantityAvailable(50).quantityReserved(0).build();

        salesOrder = SalesOrder.builder()
                .id(1L)
                .orderNumber("SO-123")
                .warehouse(warehouse)
                .status(SalesOrderStatus.CONFIRMED)
                .createdBy(user)
                .totalAmount(BigDecimal.TEN)
                .build();

        salesOrderItem = SalesOrderItem.builder()
                .id(1L)
                .salesOrder(salesOrder)
                .product(product)
                .quantity(5)
                .unitPrice(BigDecimal.TEN)
                .totalPrice(BigDecimal.valueOf(50))
                .build();

        salesOrder.setItems(List.of(salesOrderItem));
    }

    @Test
    void createSalesOrder_shouldReserveInventory_onOrderCreation() {
        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerName("Customer 1");
        request.setCustomerEmail("customer@test.com");
        request.setCustomerPhone("123456");
        request.setShippingAddress("Address 1");
        request.setWarehouseId(1L);

        SalesOrderItemRequest itemReq = new SalesOrderItemRequest();
        itemReq.setProductId(1L);
        itemReq.setQuantity(10);
        request.setItems(List.of(itemReq));

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(salesOrderItemRepository.save(any(SalesOrderItem.class))).thenReturn(salesOrderItem);

        SalesOrderResponse response = salesOrderService.createSalesOrder(request, 1L);

        assertNotNull(response);
        assertEquals(40, inventory.getQuantityAvailable()); // 50 - 10
        assertEquals(10, inventory.getQuantityReserved()); // 0 + 10
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void createSalesOrder_shouldThrowException_whenInsufficientStockForAnyItem() {
        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@test.com");
        request.setCustomerPhone("1234567890");
        request.setShippingAddress("123 Test St");
        request.setWarehouseId(1L);

        SalesOrderItemRequest itemReq = new SalesOrderItemRequest();
        itemReq.setProductId(1L);
        itemReq.setQuantity(100); // Exceeds 50
        request.setItems(List.of(itemReq));

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientInventoryException.class, () -> salesOrderService.createSalesOrder(request, 1L));
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    void createSalesOrder_shouldPublishKafkaEvent_onSuccessfulCreation() {
        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@test.com");
        request.setCustomerPhone("1234567890");
        request.setShippingAddress("123 Test St");
        request.setWarehouseId(1L);
        SalesOrderItemRequest itemReq = new SalesOrderItemRequest();
        itemReq.setProductId(1L);
        itemReq.setQuantity(10);
        request.setItems(List.of(itemReq));

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(salesOrderItemRepository.save(any(SalesOrderItem.class))).thenReturn(salesOrderItem);

        salesOrderService.createSalesOrder(request, 1L);

        verify(kafkaEventProducer, times(1)).publishSalesOrderCreated(any());
    }

    @Test
    void cancelSalesOrder_shouldReleaseReservedInventory_onCancellation() {
        inventory.setQuantityAvailable(40);
        inventory.setQuantityReserved(10);

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);

        SalesOrderResponse response = salesOrderService.cancelSalesOrder(1L, "Test reason");

        assertNotNull(response);
        assertEquals(45, inventory.getQuantityAvailable()); // 40 + 5
        assertEquals(5, inventory.getQuantityReserved()); // 10 - 5
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(kafkaEventProducer, times(1)).publishSalesOrderCancelled(any());
    }

    @Test
    void cancelSalesOrder_shouldThrowException_whenOrderIsAlreadyDispatched() {
        salesOrder.setStatus(SalesOrderStatus.DISPATCHED);
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));

        assertThrows(InvalidOperationException.class, () -> salesOrderService.cancelSalesOrder(1L, "Cancel reason"));
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    void dispatchSalesOrder_shouldCreateOutboundTransactions_forAllItems() {
        inventory.setQuantityReserved(5);
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);

        SalesOrderResponse response = salesOrderService.dispatchSalesOrder(1L, 1L);

        assertNotNull(response);
        assertEquals(0, inventory.getQuantityReserved());
        verify(transactionRepository, times(1)).save(any(InventoryTransaction.class));
    }
}
