package com.supplysync.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderCreatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long salesOrderId;
    private String orderNumber;
    private Long warehouseId;
    private BigDecimal totalAmount;
    private String timestamp;
}
