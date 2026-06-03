package com.supplysync.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Data
public class PurchaseOrderItemRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "The database ID of the product", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long productId;

    @NotNull(message = "Quantity ordered is required")
    @Min(value = 1, message = "Quantity ordered must be at least 1")
    @Schema(description = "The quantity of units ordered", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer quantityOrdered;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    @Schema(description = "The negotiation unit price cost for this order line", requiredMode = Schema.RequiredMode.REQUIRED, example = "45.00")
    private BigDecimal unitPrice;
}
