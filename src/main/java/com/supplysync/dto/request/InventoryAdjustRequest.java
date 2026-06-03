package com.supplysync.dto.request;

import com.supplysync.enums.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class InventoryAdjustRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "The database ID of the product", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long productId;

    @NotNull(message = "Warehouse ID is required")
    @Schema(description = "The database ID of the warehouse", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long warehouseId;

    @NotNull(message = "Transaction type is required")
    @Schema(description = "The type of inventory transaction (e.g. INBOUND, OUTBOUND, DAMAGE)", requiredMode = Schema.RequiredMode.REQUIRED, example = "INBOUND")
    private TransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "The quantity of units to adjust", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer quantity;

    @Schema(description = "Optional notes describing the adjustment reason", example = "Forklift driver damaged box during transit")
    private String notes;
}
