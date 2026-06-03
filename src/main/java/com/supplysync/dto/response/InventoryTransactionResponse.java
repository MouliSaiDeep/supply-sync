package com.supplysync.dto.response;

import com.supplysync.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponse {

    @Schema(description = "The database ID of the transaction record", example = "89")
    private Long id;

    @Schema(description = "The database ID of the product", example = "12")
    private Long productId;

    @Schema(description = "The stock keeping unit (SKU)", example = "MON-UW-34")
    private String productSku;

    @Schema(description = "The name of the product", example = "Ultra-Wide Monitor 34\"")
    private String productName;

    @Schema(description = "The database ID of the warehouse", example = "1")
    private Long warehouseId;

    @Schema(description = "The name of the warehouse", example = "Midwest Distribution Hub")
    private String warehouseName;

    @Schema(description = "The type of transaction", example = "DAMAGE")
    private TransactionType transactionType;

    @Schema(description = "The quantity of units adjusted", example = "5")
    private Integer quantity;

    @Schema(description = "The unique reference tracking code", example = "ADJUST-1780729960")
    private String referenceId;

    @Schema(description = "The ID of the user who performed the action", example = "3")
    private Long performedByUserId;

    @Schema(description = "The username of the performer", example = "bob_staff")
    private String performedByUsername;

    @Schema(description = "Description / notes of the transaction", example = "Monitor screen cracked during forklift transit")
    private String notes;

    @Schema(description = "The creation timestamp", example = "2026-06-06T12:50:00")
    private LocalDateTime createdAt;
}
