package com.supplysync.dto.request;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Data
public class ProductSearchRequest {

    @Schema(description = "Filter by category ID", example = "1")
    private Long categoryId;

    @Schema(description = "Filter by active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Filter by minimum unit price", example = "10.00")
    private BigDecimal minPrice;

    @Schema(description = "Filter by maximum unit price", example = "500.00")
    private BigDecimal maxPrice;

    @Schema(description = "Search query matching SKU, name, or description", example = "wireless")
    private String search;
}
