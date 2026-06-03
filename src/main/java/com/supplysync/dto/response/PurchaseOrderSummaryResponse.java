package com.supplysync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderSummaryResponse {

    @Schema(description = "The total count of purchase orders in the specified period", example = "85")
    private Long totalPOs;

    @Schema(description = "The cumulative cost value of all purchase orders", example = "125000.00")
    private BigDecimal totalValue;

    @Schema(description = "Breakdown metrics mapped by purchase order status")
    private Map<String, StatusCountValue> statusBreakdown;

    @Schema(description = "The list of top suppliers by purchase order value")
    private List<SupplierValue> topSuppliers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusCountValue {
        @Schema(description = "The count of purchase orders in this status", example = "5")
        private Long count;

        @Schema(description = "The total value of purchase orders in this status", example = "25000.00")
        private BigDecimal value;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SupplierValue {
        @Schema(description = "The database ID of the supplier", example = "4")
        private Long supplierId;

        @Schema(description = "The name of the supplier", example = "Global Tech Displays Inc")
        private String supplierName;

        @Schema(description = "The total cumulative purchase value from this supplier", example = "26000.00")
        private BigDecimal totalValue;
    }
}
