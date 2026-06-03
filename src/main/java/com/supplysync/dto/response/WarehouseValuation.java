package com.supplysync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseValuation {

    @Schema(description = "The database ID of the warehouse", example = "1")
    private Long warehouseId;

    @Schema(description = "The name of the warehouse", example = "Midwest Distribution Hub")
    private String warehouseName;

    @Schema(description = "The list of product valuations in this warehouse")
    private List<ProductValuation> products;

    @Schema(description = "The sum total value of all stock in this warehouse", example = "120000.00")
    private BigDecimal warehouseTotalValue;
}
