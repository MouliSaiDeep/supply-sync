package com.supplysync.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class PurchaseOrderReceiptItemRequest {

    @NotNull(message = "Purchase order item ID is required")
    @Schema(description = "The database ID of the purchase order line item", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long poItemId;

    @NotNull(message = "Quantity received is required")
    @Min(value = 1, message = "Quantity received must be at least 1")
    @Schema(description = "The quantity of units received", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer quantityReceived;
}
