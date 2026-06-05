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
public class SalesOrderCancelledEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long salesOrderId;
    private String orderNumber;
    private String reason;
    private String timestamp;
}
