package com.supplysync.service;

import com.supplysync.dto.request.PurchaseOrderReceiptItemRequest;
import com.supplysync.dto.request.PurchaseOrderReceiptRequest;
import com.supplysync.dto.request.PurchaseOrderRequest;
import com.supplysync.dto.response.PurchaseOrderResponse;
import com.supplysync.entity.*;
import com.supplysync.enums.PurchaseOrderStatus;
import org.springframework.security.access.AccessDeniedException;
import com.supplysync.exception.InvalidOperationException;
import com.supplysync.kafka.producer.KafkaEventProducer;
import com.supplysync.repository.*;
import com.supplysync.service.impl.PurchaseOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.concurrent.TimeUnit;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private PurchaseOrderItemRepository purchaseOrderItemRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private KafkaEventProducer kafkaEventProducer;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    private User creator;
    private User approver;
    private Supplier supplier;
    private Warehouse warehouse;
    private Product product;
    private PurchaseOrder purchaseOrder;
    private PurchaseOrderItem purchaseOrderItem;

    @BeforeEach
    void setUp() {
        creator = User.builder().id(1L).username("creator").build();
        approver = User.builder().id(2L).username("approver").build();
        supplier = Supplier.builder().id(1L).supplierCode("SUP-123").name("Supplier 1").build();
        warehouse = Warehouse.builder().id(1L).warehouseCode("WH-123").name("Warehouse 1").build();
        product = Product.builder().id(1L).sku("SKU-123").name("Test Product").unitPrice(BigDecimal.TEN).build();

        purchaseOrder = PurchaseOrder.builder()
                .id(1L)
                .poNumber("PO-20260605-0001")
                .supplier(supplier)
                .warehouse(warehouse)
                .status(PurchaseOrderStatus.DRAFT)
                .createdBy(creator)
                .totalAmount(BigDecimal.TEN)
                .build();

        purchaseOrderItem = PurchaseOrderItem.builder()
                .id(1L)
                .purchaseOrder(purchaseOrder)
                .product(product)
                .quantityOrdered(10)
                .quantityReceived(0)
                .unitPrice(BigDecimal.TEN)
                .totalPrice(BigDecimal.valueOf(100))
                .build();

        purchaseOrder.setItems(List.of(purchaseOrderItem));
    }

    @Test
    void createPurchaseOrder_shouldGeneratePoNumber_withCorrectFormat() {
        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(creator);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(1L);
        request.setWarehouseId(1L);
        request.setExpectedDeliveryDate(LocalDate.now());

        var itemReq = new com.supplysync.dto.request.PurchaseOrderItemRequest();
        itemReq.setProductId(1L);
        itemReq.setQuantityOrdered(10);
        itemReq.setUnitPrice(BigDecimal.TEN);
        request.setItems(List.of(itemReq));

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderItemRepository.save(any(PurchaseOrderItem.class))).thenReturn(purchaseOrderItem);

        try {
            PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);

            assertNotNull(response);
            assertTrue(response.getPoNumber().startsWith("PO-"));
            assertEquals(16, response.getPoNumber().length()); // e.g. PO-20260605-0001
            verify(purchaseOrderRepository, times(2)).save(any(PurchaseOrder.class));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void approvePurchaseOrder_shouldThrowException_whenApproverIsSameAsCreator() {
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        assertThrows(AccessDeniedException.class, () -> purchaseOrderService.approvePurchaseOrder(1L, 1L)); // Approver ID = 1L (same as creator)
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void approvePurchaseOrder_shouldThrowException_whenPoIsNotInPendingApprovalStatus() {
        // status is DRAFT
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        assertThrows(InvalidOperationException.class, () -> purchaseOrderService.approvePurchaseOrder(1L, 2L));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void receivePurchaseOrder_shouldUpdateInventory_forReceivedItems() {
        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);

        PurchaseOrderReceiptRequest request = new PurchaseOrderReceiptRequest();
        request.setActualDeliveryDate(LocalDate.now());

        PurchaseOrderReceiptItemRequest receiptItem = new PurchaseOrderReceiptItemRequest();
        receiptItem.setPoItemId(1L);
        receiptItem.setQuantityReceived(10); // fully received
        request.setItems(List.of(receiptItem));

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderItemRepository.save(any(PurchaseOrderItem.class))).thenReturn(purchaseOrderItem);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);

        PurchaseOrderResponse response = purchaseOrderService.receivePurchaseOrder(1L, request, 2L);

        assertNotNull(response);
        assertEquals(10, purchaseOrderItem.getQuantityReceived());
        verify(inventoryService, times(1)).adjustInventory(any(), eq(2L));
        verify(kafkaEventProducer, times(1)).publishPurchaseOrderReceived(any());
    }

    @Test
    void receivePurchaseOrder_shouldSetStatusToPartiallyReceived_whenNotAllItemsReceived() {
        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);

        PurchaseOrderReceiptRequest request = new PurchaseOrderReceiptRequest();
        request.setActualDeliveryDate(LocalDate.now());

        PurchaseOrderReceiptItemRequest receiptItem = new PurchaseOrderReceiptItemRequest();
        receiptItem.setPoItemId(1L);
        receiptItem.setQuantityReceived(4); // partially received (ordered 10)
        request.setItems(List.of(receiptItem));

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderItemRepository.save(any(PurchaseOrderItem.class))).thenReturn(purchaseOrderItem);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);

        PurchaseOrderResponse response = purchaseOrderService.receivePurchaseOrder(1L, request, 2L);

        assertNotNull(response);
        assertEquals(PurchaseOrderStatus.PARTIALLY_RECEIVED, purchaseOrder.getStatus());
    }

    @Test
    void cancelPurchaseOrder_shouldThrowException_whenPoIsInReceivedStatus() {
        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        assertThrows(InvalidOperationException.class, () -> purchaseOrderService.cancelPurchaseOrder(1L, "Reason"));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }
}
