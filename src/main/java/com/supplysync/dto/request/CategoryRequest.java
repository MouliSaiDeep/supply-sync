package com.supplysync.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class CategoryRequest {

    @Size(max = 20, message = "Category code must not exceed 20 characters")
    @Schema(description = "The unique code of the category", example = "CAT-ELEC")
    private String categoryCode;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Schema(description = "The name of the category", requiredMode = Schema.RequiredMode.REQUIRED, example = "Electronics")
    private String name;

    @Schema(description = "Description of the category", example = "Electronic devices and computer parts")
    private String description;

    @Schema(description = "The database ID of the parent category if nested", example = "1")
    private Long parentCategoryId;
}
