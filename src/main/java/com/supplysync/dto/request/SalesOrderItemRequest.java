package com.supplysync.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class SalesOrderItemRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "The database ID of the product ordered", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "The quantity of units ordered", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer quantity;
}
