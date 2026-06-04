package com.supplysync.service.impl;

import com.supplysync.dto.response.*;
import com.supplysync.entity.*;
import com.supplysync.enums.PurchaseOrderStatus;
import com.supplysync.enums.SalesOrderStatus;
import com.supplysync.enums.TransactionType;
import com.supplysync.exception.ResourceNotFoundException;
import com.supplysync.mapper.EntityMapper;
import com.supplysync.repository.*;
import com.supplysync.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryRepository inventoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "reports:dashboard", key = "''")
    public DashboardResponse getDashboardSummary() {
        log.info("Generating dashboard summary report");

        long totalWarehouses = warehouseRepository.count();
        long totalProducts = productRepository.count();
        long totalSuppliers = supplierRepository.count();

        // Calculate total inventory value globally
        BigDecimal totalInventoryValue = inventoryRepository.findAll().stream()
                .map(inv -> BigDecimal.valueOf(inv.getQuantityAvailable()).multiply(inv.getProduct().getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Open purchase orders count
        long openPO = purchaseOrderRepository.countByStatus(PurchaseOrderStatus.PENDING_APPROVAL)
                + purchaseOrderRepository.countByStatus(PurchaseOrderStatus.APPROVED)
                + purchaseOrderRepository.countByStatus(PurchaseOrderStatus.ORDERED)
                + purchaseOrderRepository.countByStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);

        // Pending sales orders count (Pending + Confirmed + Processing)
        long pendingSO = salesOrderRepository.countByStatus(SalesOrderStatus.PENDING)
                + salesOrderRepository.countByStatus(SalesOrderStatus.CONFIRMED)
                + salesOrderRepository.countByStatus(SalesOrderStatus.PROCESSING);

        // Low stock count (distinct products)
        long lowStockCount = inventoryRepository.findLowStockInventory().stream()
                .map(i -> i.getProduct().getId())
                .distinct()
                .count();

        // Top 5 selling products in last 30 days
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(30);
        List<Object[]> topSellingRows = salesOrderItemRepository.findTopSellingProducts(sinceDate, PageRequest.of(0, 5));
        List<TopSellingProductResponse> topSellingProducts = topSellingRows.stream()
                .map(row -> TopSellingProductResponse.builder()
                        .productId((Long) row[0])
                        .sku((String) row[1])
                        .productName((String) row[2])
                        .totalQuantity((Long) row[3])
                        .build())
                .collect(Collectors.toList());

        // Recent 10 inventory transactions
        List<InventoryTransactionResponse> recentTransactions = transactionRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(EntityMapper::toTransactionResponse)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalWarehouses(totalWarehouses)
                .totalProducts(totalProducts)
                .totalSuppliers(totalSuppliers)
                .totalInventoryValue(totalInventoryValue)
                .openPurchaseOrders(openPO)
                .pendingSalesOrders(pendingSO)
                .lowStockProductCount(lowStockCount)
                .topSellingProducts(topSellingProducts)
                .recentTransactions(recentTransactions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryValuationResponse getInventoryValuation(Long warehouseId) {
        log.info("Generating inventory valuation report - warehouse ID: {}", warehouseId);
        List<WarehouseValuation> warehouseValuations = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        if (warehouseId != null) {
            Warehouse warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + warehouseId));
            WarehouseValuation valuation = calculateWarehouseValuation(warehouse);
            warehouseValuations.add(valuation);
            grandTotal = valuation.getWarehouseTotalValue();
        } else {
            List<Warehouse> warehouses = warehouseRepository.findAll();
            for (Warehouse wh : warehouses) {
                WarehouseValuation valuation = calculateWarehouseValuation(wh);
                warehouseValuations.add(valuation);
                grandTotal = grandTotal.add(valuation.getWarehouseTotalValue());
            }
        }

        return InventoryValuationResponse.builder()
                .warehouses(warehouseValuations)
                .grandTotalValue(grandTotal)
                .build();
    }

    private WarehouseValuation calculateWarehouseValuation(Warehouse warehouse) {
        List<Inventory> inventories = inventoryRepository.findByWarehouseId(warehouse.getId());
        List<ProductValuation> productsValuation = inventories.stream()
                .map(inv -> {
                    BigDecimal totalValue = inv.getProduct().getUnitPrice().multiply(BigDecimal.valueOf(inv.getQuantityAvailable()));
                    return ProductValuation.builder()
                            .sku(inv.getProduct().getSku())
                            .productName(inv.getProduct().getName())
                            .quantityAvailable(inv.getQuantityAvailable())
                            .unitPrice(inv.getProduct().getUnitPrice())
                            .totalValue(totalValue)
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal totalVal = productsValuation.stream()
                .map(ProductValuation::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return WarehouseValuation.builder()
                .warehouseId(warehouse.getId())
                .warehouseName(warehouse.getName())
                .products(productsValuation)
                .warehouseTotalValue(totalVal)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderSummaryResponse getPurchaseOrderSummary(LocalDate startDate, LocalDate endDate, Long supplierId, String status) {
        log.info("Generating PO summary report - period: {} to {}, supplier: {}, status: {}", startDate, endDate, supplierId, status);

        PurchaseOrderStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            statusEnum = PurchaseOrderStatus.valueOf(status.toUpperCase());
        }

        List<PurchaseOrder> pos = purchaseOrderRepository.findForSummaryReport(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                supplierId,
                statusEnum
        );

        long totalPOs = pos.size();
        BigDecimal totalValue = pos.stream()
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Status Breakdown
        Map<String, PurchaseOrderSummaryResponse.StatusCountValue> breakdown = new HashMap<>();
        for (PurchaseOrder po : pos) {
            String statusStr = po.getStatus().name();
            PurchaseOrderSummaryResponse.StatusCountValue stats = breakdown.computeIfAbsent(statusStr,
                    k -> new PurchaseOrderSummaryResponse.StatusCountValue(0L, BigDecimal.ZERO));
            stats.setCount(stats.getCount() + 1);
            stats.setValue(stats.getValue().add(po.getTotalAmount()));
        }

        // Top 5 Suppliers by PO value
        Map<Long, PurchaseOrderSummaryResponse.SupplierValue> supplierMap = new HashMap<>();
        for (PurchaseOrder po : pos) {
            Supplier s = po.getSupplier();
            PurchaseOrderSummaryResponse.SupplierValue val = supplierMap.computeIfAbsent(s.getId(),
                    k -> new PurchaseOrderSummaryResponse.SupplierValue(s.getId(), s.getName(), BigDecimal.ZERO));
            val.setTotalValue(val.getTotalValue().add(po.getTotalAmount()));
        }

        List<PurchaseOrderSummaryResponse.SupplierValue> topSuppliers = supplierMap.values().stream()
                .sorted((v1, v2) -> v2.getTotalValue().compareTo(v1.getTotalValue()))
                .limit(5)
                .collect(Collectors.toList());

        return PurchaseOrderSummaryResponse.builder()
                .totalPOs(totalPOs)
                .totalValue(totalValue)
                .statusBreakdown(breakdown)
                .topSuppliers(topSuppliers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderSummaryResponse getSalesOrderSummary(LocalDate startDate, LocalDate endDate, Long warehouseId, String status) {
        log.info("Generating Sales Order summary report - period: {} to {}, warehouse: {}, status: {}", startDate, endDate, warehouseId, status);

        SalesOrderStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            statusEnum = SalesOrderStatus.valueOf(status.toUpperCase());
        }

        List<SalesOrder> orders = salesOrderRepository.findForSummaryReport(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                warehouseId,
                statusEnum
        );

        long totalOrders = orders.size();

        // Total Revenue (sum of DELIVERED orders only)
        List<SalesOrder> deliveredOrders = orders.stream()
                .filter(o -> o.getStatus() == SalesOrderStatus.DELIVERED)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = deliveredOrders.stream()
                .map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Average Order Value (AOV) of delivered orders
        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (!deliveredOrders.isEmpty()) {
            averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(deliveredOrders.size()), 2, RoundingMode.HALF_UP);
        }

        // Status Breakdown
        Map<String, SalesOrderSummaryResponse.StatusCountValue> breakdown = new HashMap<>();
        for (SalesOrder o : orders) {
            String statusStr = o.getStatus().name();
            SalesOrderSummaryResponse.StatusCountValue stats = breakdown.computeIfAbsent(statusStr,
                    k -> new SalesOrderSummaryResponse.StatusCountValue(0L, BigDecimal.ZERO));
            stats.setCount(stats.getCount() + 1);
            stats.setValue(stats.getValue().add(o.getTotalAmount()));
        }

        // Top 5 Products by Revenue (summing quantities and prices for DELIVERED orders)
        Map<Long, SalesOrderSummaryResponse.ProductRevenue> productRevenueMap = new HashMap<>();
        for (SalesOrder o : deliveredOrders) {
            for (SalesOrderItem item : o.getItems()) {
                Product p = item.getProduct();
                SalesOrderSummaryResponse.ProductRevenue val = productRevenueMap.computeIfAbsent(p.getId(),
                        k -> new SalesOrderSummaryResponse.ProductRevenue(p.getId(), p.getSku(), p.getName(), BigDecimal.ZERO));
                val.setTotalRevenue(val.getTotalRevenue().add(item.getTotalPrice()));
            }
        }

        List<SalesOrderSummaryResponse.ProductRevenue> topProducts = productRevenueMap.values().stream()
                .sorted((v1, v2) -> v2.getTotalRevenue().compareTo(v1.getTotalRevenue()))
                .limit(5)
                .collect(Collectors.toList());

        return SalesOrderSummaryResponse.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .statusBreakdown(breakdown)
                .topProducts(topProducts)
                .build();
    }
}
