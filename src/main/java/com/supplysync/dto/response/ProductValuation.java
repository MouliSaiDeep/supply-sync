package com.supplysync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductValuation {

    @Schema(description = "The stock keeping unit (SKU) of the product", example = "MON-UW-34")
    private String sku;

    @Schema(description = "The name of the product", example = "Ultra-Wide Monitor 34\"")
    private String productName;

    @Schema(description = "The quantity available in stock", example = "45")
    private Integer quantityAvailable;

    @Schema(description = "The product unit price", example = "499.99")
    private BigDecimal unitPrice;

    @Schema(description = "The total asset value of this product stock (quantity * price)", example = "22499.55")
    private BigDecimal totalValue;
}
