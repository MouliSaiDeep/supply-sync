package com.supplysync.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @Size(max = 50, message = "SKU must not exceed 50 characters")
    @Schema(description = "The stock keeping unit (SKU) of the product", example = "PROD-100-BLUE")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    @Schema(description = "The name of the product", requiredMode = Schema.RequiredMode.REQUIRED, example = "Blue Wireless Mouse")
    private String name;

    @Schema(description = "Product description", example = "Ergonomic wireless mouse with 5 adjustable DPI levels")
    private String description;

    @NotNull(message = "Category ID is required")
    @Schema(description = "The ID of the category this product belongs to", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long categoryId;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    @Schema(description = "The unit price of the product", requiredMode = Schema.RequiredMode.REQUIRED, example = "29.99")
    private BigDecimal unitPrice;

    @NotBlank(message = "Unit of measure is required")
    @Size(max = 20, message = "Unit of measure must not exceed 20 characters")
    @Schema(description = "The unit of measurement", requiredMode = Schema.RequiredMode.REQUIRED, example = "pieces")
    private String unitOfMeasure;

    @Min(value = 0, message = "Reorder level must be at least 0")
    @Schema(description = "The inventory reorder threshold", example = "50")
    private Integer reorderLevel = 0;

    @Schema(description = "Whether the product is active", example = "true")
    private Boolean isActive = true;
}
