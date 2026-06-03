package com.supplysync.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class InventoryTransferRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "The ID of the product being transferred", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long productId;

    @NotNull(message = "Source warehouse ID is required")
    @Schema(description = "The ID of the source warehouse", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long sourceWarehouseId;

    @NotNull(message = "Destination warehouse ID is required")
    @Schema(description = "The ID of the destination warehouse", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long destinationWarehouseId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "The quantity of units to transfer", requiredMode = Schema.RequiredMode.REQUIRED, example = "150")
    private Integer quantity;

    @Schema(description = "Optional transfer notes", example = "Redistribute extra stock for winter season sale")
    private String notes;
}
