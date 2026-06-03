package com.supplysync.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderRequest {

    @NotNull(message = "Supplier ID is required")
    @Schema(description = "The database ID of the supplier", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long supplierId;

    @NotNull(message = "Warehouse ID is required")
    @Schema(description = "The database ID of the destination warehouse", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long warehouseId;

    @Schema(description = "The expected delivery date of the purchase order", example = "2026-08-01")
    private LocalDate expectedDeliveryDate;

    @Schema(description = "Optional notes or terms for the purchase order", example = "Deliver after 9 AM only")
    private String notes;

    @NotEmpty(message = "Purchase order must have at least one line item")
    @Valid
    @Schema(description = "The list of items to purchase", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PurchaseOrderItemRequest> items;
}
