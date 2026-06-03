package com.supplysync.dto.response;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDetailResponse {

    @Schema(description = "The database ID of the warehouse", example = "1")
    private Long id;

    @Schema(description = "The unique code of the warehouse", example = "WH-MIDWEST-01")
    private String warehouseCode;

    @Schema(description = "The name of the warehouse", example = "Midwest Distribution Hub")
    private String name;

    @Schema(description = "The address of the warehouse", example = "404 Logistics Boulevard")
    private String location;

    @Schema(description = "The city of the warehouse", example = "Chicago")
    private String city;

    @Schema(description = "The state of the warehouse", example = "IL")
    private String state;

    @Schema(description = "The postal code of the warehouse", example = "60666")
    private String pincode;

    @Schema(description = "The storage capacity volume", example = "50000")
    private Integer capacity;

    @Schema(description = "Whether the warehouse is active", example = "true")
    private Boolean isActive;

    @Schema(description = "The creation timestamp", example = "2026-06-06T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "The last updated timestamp", example = "2026-06-06T12:50:00")
    private LocalDateTime updatedAt;

    @Schema(description = "The number of unique products currently stored in this warehouse", example = "142")
    private Long totalProductsStored;

    @Schema(description = "The total cumulative quantity of all stock items in this warehouse", example = "24050")
    private Long totalQuantity;
}
