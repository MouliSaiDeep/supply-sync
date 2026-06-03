package com.supplysync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlertResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "The database ID of the product", example = "12")
    private Long productId;

    @Schema(description = "The stock keeping unit (SKU)", example = "MON-UW-34")
    private String sku;

    @Schema(description = "The name of the product", example = "Ultra-Wide Monitor 34\"")
    private String productName;

    @Schema(description = "The database ID of the warehouse", example = "1")
    private Long warehouseId;

    @Schema(description = "The name of the warehouse", example = "Midwest Distribution Hub")
    private String warehouseName;

    @Schema(description = "The quantity available in stock", example = "12")
    private Integer quantityAvailable;

    @Schema(description = "The reorder threshold quantity", example = "15")
    private Integer reorderLevel;

    @Schema(description = "The quantity deficit (reorder level - available quantity)", example = "3")
    private Integer deficit;
}
