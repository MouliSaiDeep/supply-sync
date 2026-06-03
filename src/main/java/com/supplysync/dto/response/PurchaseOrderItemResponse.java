package com.supplysync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemResponse {

    @Schema(description = "The database ID of the purchase order line item", example = "15")
    private Long id;

    @Schema(description = "The database ID of the product", example = "12")
    private Long productId;

    @Schema(description = "The stock keeping unit (SKU) of the product", example = "MON-UW-34")
    private String productSku;

    @Schema(description = "The name of the product", example = "Ultra-Wide Monitor 34\"")
    private String productName;

    @Schema(description = "The quantity of units ordered", example = "100")
    private Integer quantityOrdered;

    @Schema(description = "The quantity of units already received", example = "100")
    private Integer quantityReceived;

    @Schema(description = "The cost unit price negotiated", example = "260.00")
    private BigDecimal unitPrice;

    @Schema(description = "The total cost for this order line item", example = "26000.00")
    private BigDecimal totalPrice;

    @Schema(description = "The creation timestamp", example = "2026-06-06T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "The last updated timestamp", example = "2026-06-06T12:50:00")
    private LocalDateTime updatedAt;
}
