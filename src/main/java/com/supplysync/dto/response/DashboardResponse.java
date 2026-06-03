package com.supplysync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "The total count of warehouses", example = "5")
    private Long totalWarehouses;

    @Schema(description = "The total count of registered products", example = "150")
    private Long totalProducts;

    @Schema(description = "The total count of active suppliers", example = "20")
    private Long totalSuppliers;

    @Schema(description = "The total asset value of inventory globally", example = "245000.99")
    private BigDecimal totalInventoryValue;

    @Schema(description = "The count of open purchase orders", example = "8")
    private Long openPurchaseOrders;

    @Schema(description = "The count of pending sales orders", example = "12")
    private Long pendingSalesOrders;

    @Schema(description = "The count of products currently low in stock", example = "3")
    private Long lowStockProductCount;

    @Schema(description = "The list of top-selling products by quantity and revenue")
    private List<TopSellingProductResponse> topSellingProducts;

    @Schema(description = "The list of recent inventory transactions")
    private List<InventoryTransactionResponse> recentTransactions;
}
