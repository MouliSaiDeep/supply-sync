package com.supplysync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeResponse {

    @Schema(description = "The database ID of the category", example = "2")
    private Long id;

    @Schema(description = "The unique code of the category", example = "CAT-ELEC")
    private String categoryCode;

    @Schema(description = "The name of the category", example = "Electronics")
    private String name;

    @Schema(description = "The category description", example = "Electronic appliances and components")
    private String description;

    @Builder.Default
    @Schema(description = "The list of subcategories nested under this category")
    private List<CategoryTreeResponse> children = new ArrayList<>();
}
