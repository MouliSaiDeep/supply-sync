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
public class InventorySnapshotResponse {

    @Schema(description = "The database ID of the inventory record", example = "1")
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

    @Schema(description = "The quantity available for new orders", example = "45")
    private Integer quantityAvailable;

    @Schema(description = "The quantity reserved for pending orders", example = "5")
    private Integer quantityReserved;

    @Schema(description = "The quantity of damaged stock units", example = "2")
    private Integer quantityDamaged;

    @Schema(description = "The last updated timestamp", example = "2026-06-06T12:50:00")
    private LocalDateTime lastUpdatedAt;
}
