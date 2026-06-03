package com.supplysync.dto.response;

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
public class InventoryTransferResponse {

    @Schema(description = "The database ID of the product", example = "12")
    private Long productId;

    @Schema(description = "The stock keeping unit (SKU)", example = "MON-UW-34")
    private String productSku;

    @Schema(description = "The name of the product", example = "Ultra-Wide Monitor 34\"")
    private String productName;

    @Schema(description = "The database ID of the source warehouse", example = "1")
    private Long sourceWarehouseId;

    @Schema(description = "The name of the source warehouse", example = "Midwest Distribution Hub")
    private String sourceWarehouseName;

    @Schema(description = "The database ID of the destination warehouse", example = "2")
    private Long destinationWarehouseId;

    @Schema(description = "The name of the destination warehouse", example = "East Coast Hub")
    private String destinationWarehouseName;

    @Schema(description = "The quantity of units transferred", example = "10")
    private Integer quantity;

    @Schema(description = "Optional notes regarding the transfer", example = "Stock rebalancing")
    private String notes;

    @Schema(description = "The timestamp when transfer was processed", example = "2026-06-06T12:51:30")
    private LocalDateTime createdAt;
}
