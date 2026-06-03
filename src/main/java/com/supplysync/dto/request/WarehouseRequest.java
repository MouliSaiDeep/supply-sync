package com.supplysync.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class WarehouseRequest {

    @Size(max = 20, message = "Warehouse code must not exceed 20 characters")
    @Schema(description = "The unique code of the warehouse", example = "WH-MIDWEST-01")
    private String warehouseCode;

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 150, message = "Warehouse name must not exceed 150 characters")
    @Schema(description = "The name of the warehouse", requiredMode = Schema.RequiredMode.REQUIRED, example = "Midwest Distribution Hub")
    private String name;

    @NotBlank(message = "Location is required")
    @Schema(description = "The address / location of the warehouse", requiredMode = Schema.RequiredMode.REQUIRED, example = "404 Logistics Boulevard")
    private String location;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "The city where the warehouse is located", requiredMode = Schema.RequiredMode.REQUIRED, example = "Chicago")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    @Schema(description = "The state where the warehouse is located", requiredMode = Schema.RequiredMode.REQUIRED, example = "IL")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Size(max = 10, message = "Pincode must not exceed 10 characters")
    @Schema(description = "The pin/postal code of the warehouse", requiredMode = Schema.RequiredMode.REQUIRED, example = "60666")
    private String pincode;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Schema(description = "The maximum volume capacity of the warehouse", requiredMode = Schema.RequiredMode.REQUIRED, example = "50000")
    private Integer capacity;

    @Schema(description = "Whether the warehouse is active", example = "true")
    private Boolean isActive = true;
}
