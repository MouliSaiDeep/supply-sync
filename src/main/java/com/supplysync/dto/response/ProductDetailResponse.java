package com.supplysync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    @Schema(description = "The database ID of the product", example = "12")
    private Long id;

    @Schema(description = "The stock keeping unit (SKU)", example = "MON-UW-34")
    private String sku;

    @Schema(description = "The name of the product", example = "Ultra-Wide Monitor 34\"")
    private String name;

    @Schema(description = "The product description", example = "34-inch QHD curved workstation monitor")
    private String description;

    @Schema(description = "The category ID", example = "2")
    private Long categoryId;

    @Schema(description = "The category name", example = "Electronics")
    private String categoryName;

    @Schema(description = "The unit price of the product", example = "499.99")
    private BigDecimal unitPrice;

    @Schema(description = "The unit of measurement", example = "pieces")
    private String unitOfMeasure;

    @Schema(description = "The reorder stock threshold", example = "15")
    private Integer reorderLevel;

    @Schema(description = "Whether the product is currently active", example = "true")
    private Boolean isActive;

    @Schema(description = "The creation timestamp", example = "2026-06-06T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "The last updated timestamp", example = "2026-06-06T12:50:00")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Schema(description = "The stock breakdown across different warehouses")
    private List<InventorySummary> inventoryByWarehouse = new ArrayList<>();
}
