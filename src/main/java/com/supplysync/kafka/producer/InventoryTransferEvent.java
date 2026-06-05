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
public class InventoryTransferEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private Long sourceWarehouseId;
    private Long destinationWarehouseId;
    private Integer quantity;
    private String notes;
    private Long performedByUserId;
    private String timestamp;
}
