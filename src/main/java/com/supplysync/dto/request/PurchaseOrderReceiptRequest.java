package com.supplysync.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderReceiptRequest {

    @NotEmpty(message = "Receipt must contain at least one item")
    @Valid
    @Schema(description = "The list of items received", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PurchaseOrderReceiptItemRequest> items;

    @NotNull(message = "Actual delivery date is required")
    @Schema(description = "The actual date when delivery was received", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-06-06")
    private LocalDate actualDeliveryDate;
}
