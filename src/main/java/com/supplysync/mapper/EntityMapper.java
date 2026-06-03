package com.supplysync.mapper;

import com.supplysync.dto.response.*;
import com.supplysync.entity.*;

import java.util.List;
import java.util.stream.Collectors;

public class EntityMapper {

    public static WarehouseResponse toResponse(Warehouse warehouse) {
        if (warehouse == null) return null;
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .warehouseCode(warehouse.getWarehouseCode())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .city(warehouse.getCity())
                .state(warehouse.getState())
                .pincode(warehouse.getPincode())
                .capacity(warehouse.getCapacity())
                .isActive(warehouse.getIsActive())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }

    public static WarehouseDetailResponse toDetailResponse(Warehouse warehouse, long totalProducts, long totalQuantity) {
        if (warehouse == null) return null;
        return WarehouseDetailResponse.builder()
                .id(warehouse.getId())
                .warehouseCode(warehouse.getWarehouseCode())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .city(warehouse.getCity())
                .state(warehouse.getState())
                .pincode(warehouse.getPincode())
                .capacity(warehouse.getCapacity())
                .isActive(warehouse.getIsActive())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .totalProductsStored(totalProducts)
                .totalQuantity(totalQuantity)
                .build();
    }

    public static CategoryResponse toResponse(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .categoryCode(category.getCategoryCode())
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .parentCategoryName(category.getParentCategory() != null ? category.getParentCategory().getName() : null)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public static CategoryTreeResponse toTreeResponse(Category category) {
        if (category == null) return null;
        return CategoryTreeResponse.builder()
                .id(category.getId())
                .categoryCode(category.getCategoryCode())
                .name(category.getName())
                .description(category.getDescription())
                .children(category.getChildren() != null ? 
                        category.getChildren().stream()
                                .map(EntityMapper::toTreeResponse)
                                .collect(Collectors.toList()) : List.of())
                .build();
    }

    public static ProductResponse toResponse(Product product) {
        if (product == null) return null;
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .unitPrice(product.getUnitPrice())
                .unitOfMeasure(product.getUnitOfMeasure())
                .reorderLevel(product.getReorderLevel())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static ProductDetailResponse toDetailResponse(Product product, List<InventorySummary> inventorySummaries) {
        if (product == null) return null;
        return ProductDetailResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .unitPrice(product.getUnitPrice())
                .unitOfMeasure(product.getUnitOfMeasure())
                .reorderLevel(product.getReorderLevel())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .inventoryByWarehouse(inventorySummaries != null ? inventorySummaries : List.of())
                .build();
    }

    public static InventorySummary toSummary(Inventory inventory) {
        if (inventory == null) return null;
        return InventorySummary.builder()
                .warehouseId(inventory.getWarehouse().getId())
                .warehouseName(inventory.getWarehouse().getName())
                .quantityAvailable(inventory.getQuantityAvailable())
                .quantityReserved(inventory.getQuantityReserved())
                .build();
    }

    public static InventorySnapshotResponse toSnapshotResponse(Inventory inventory) {
        if (inventory == null) return null;
        return InventorySnapshotResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .productSku(inventory.getProduct().getSku())
                .productName(inventory.getProduct().getName())
                .warehouseId(inventory.getWarehouse().getId())
                .warehouseName(inventory.getWarehouse().getName())
                .quantityAvailable(inventory.getQuantityAvailable())
                .quantityReserved(inventory.getQuantityReserved())
                .quantityDamaged(inventory.getQuantityDamaged())
                .lastUpdatedAt(inventory.getLastUpdatedAt())
                .build();
    }

    public static InventoryTransactionResponse toTransactionResponse(InventoryTransaction tx) {
        if (tx == null) return null;
        return InventoryTransactionResponse.builder()
                .id(tx.getId())
                .productId(tx.getProduct().getId())
                .productSku(tx.getProduct().getSku())
                .productName(tx.getProduct().getName())
                .warehouseId(tx.getWarehouse().getId())
                .warehouseName(tx.getWarehouse().getName())
                .transactionType(tx.getTransactionType())
                .quantity(tx.getQuantity())
                .referenceId(tx.getReferenceId())
                .performedByUserId(tx.getPerformedBy().getId())
                .performedByUsername(tx.getPerformedBy().getUsername())
                .notes(tx.getNotes())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    public static SupplierResponse toResponse(Supplier supplier) {
        if (supplier == null) return null;
        return SupplierResponse.builder()
                .id(supplier.getId())
                .supplierCode(supplier.getSupplierCode())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .city(supplier.getCity())
                .state(supplier.getState())
                .pincode(supplier.getPincode())
                .gstin(supplier.getGstin())
                .isActive(supplier.getIsActive())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }

    public static PurchaseOrderResponse toResponse(PurchaseOrder po) {
        if (po == null) return null;
        return PurchaseOrderResponse.builder()
                .id(po.getId())
                .poNumber(po.getPoNumber())
                .supplierId(po.getSupplier().getId())
                .supplierName(po.getSupplier().getName())
                .warehouseId(po.getWarehouse().getId())
                .warehouseName(po.getWarehouse().getName())
                .status(po.getStatus())
                .totalAmount(po.getTotalAmount())
                .expectedDeliveryDate(po.getExpectedDeliveryDate())
                .actualDeliveryDate(po.getActualDeliveryDate())
                .createdByUserId(po.getCreatedBy().getId())
                .createdByUsername(po.getCreatedBy().getUsername())
                .approvedByUserId(po.getApprovedBy() != null ? po.getApprovedBy().getId() : null)
                .approvedByUsername(po.getApprovedBy() != null ? po.getApprovedBy().getUsername() : null)
                .notes(po.getNotes())
                .items(po.getItems() != null ? po.getItems().stream()
                        .map(EntityMapper::toResponse)
                        .collect(Collectors.toList()) : List.of())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public static PurchaseOrderItemResponse toResponse(PurchaseOrderItem item) {
        if (item == null) return null;
        return PurchaseOrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productSku(item.getProduct().getSku())
                .productName(item.getProduct().getName())
                .quantityOrdered(item.getQuantityOrdered())
                .quantityReceived(item.getQuantityReceived())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public static SalesOrderResponse toResponse(SalesOrder so) {
        if (so == null) return null;
        return SalesOrderResponse.builder()
                .id(so.getId())
                .orderNumber(so.getOrderNumber())
                .customerName(so.getCustomerName())
                .customerEmail(so.getCustomerEmail())
                .customerPhone(so.getCustomerPhone())
                .shippingAddress(so.getShippingAddress())
                .warehouseId(so.getWarehouse().getId())
                .warehouseName(so.getWarehouse().getName())
                .status(so.getStatus())
                .totalAmount(so.getTotalAmount())
                .dispatchedAt(so.getDispatchedAt())
                .deliveredAt(so.getDeliveredAt())
                .createdByUserId(so.getCreatedBy().getId())
                .createdByUsername(so.getCreatedBy().getUsername())
                .notes(so.getNotes())
                .items(so.getItems() != null ? so.getItems().stream()
                        .map(EntityMapper::toResponse)
                        .collect(Collectors.toList()) : List.of())
                .createdAt(so.getCreatedAt())
                .updatedAt(so.getUpdatedAt())
                .build();
    }

    public static SalesOrderItemResponse toResponse(SalesOrderItem item) {
        if (item == null) return null;
        return SalesOrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productSku(item.getProduct().getSku())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
