package com.supplysync.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlertEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private String sku;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private Integer quantityAvailable;
    private Integer reorderLevel;
    private Integer deficit;
    private String timestamp;
}
