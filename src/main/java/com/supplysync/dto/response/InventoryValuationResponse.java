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
public class InventoryValuationResponse {

    @Schema(description = "The list of stock valuations broken down by warehouse")
    private List<WarehouseValuation> warehouses;

    @Schema(description = "The sum total value of all inventory combined globally", example = "999999.99")
    private BigDecimal grandTotalValue;
}
