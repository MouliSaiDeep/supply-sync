package com.supplysync.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private Long warehouseId;
    private String transactionType;
    private Integer quantity;
    private String notes;
    private Long performedByUserId;
    private String timestamp;
}
