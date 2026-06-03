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
public class CategoryResponse {

    @Schema(description = "The database ID of the category", example = "5")
    private Long id;

    @Schema(description = "The unique code of the category", example = "CAT-LAPTOP-01")
    private String categoryCode;

    @Schema(description = "The name of the category", example = "Laptops")
    private String name;

    @Schema(description = "The category description", example = "Portable personal computers")
    private String description;

    @Schema(description = "The ID of the parent category if nested", example = "2")
    private Long parentCategoryId;

    @Schema(description = "The name of the parent category", example = "Electronics")
    private String parentCategoryName;

    @Schema(description = "The creation timestamp", example = "2026-06-06T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "The last updated timestamp", example = "2026-06-06T12:50:00")
    private LocalDateTime updatedAt;
}
