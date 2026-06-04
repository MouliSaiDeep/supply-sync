package com.supplysync.service;

import com.supplysync.dto.request.SalesOrderRequest;
import com.supplysync.dto.response.SalesOrderResponse;

public interface SalesOrderService {
    /**
     * Creates a sales order in CONFIRMED status, checks stock availability, reserves quantity, and triggers Kafka event.
     * @param request the order details
     * @param createdByUserId user ID
     * @return the created sales order response
     */
    SalesOrderResponse createSalesOrder(SalesOrderRequest request, Long createdByUserId);

    /**
     * Dispatches a sales order, set dispatched_at, logs outbound transactions, and fulfills reservations.
     * @param id order ID
     * @return updated sales order response
     */
    SalesOrderResponse dispatchSalesOrder(Long id);
    SalesOrderResponse dispatchSalesOrder(Long id, Long performedByUserId);

    /**
     * Marks a sales order as delivered.
     * @param id order ID
     * @return updated sales order response
     */
    SalesOrderResponse deliverSalesOrder(Long id);

    /**
     * Cancels a sales order, releases reservations, and triggers Kafka event.
     * @param id order ID
     * @param reason cancellation reason
     * @return updated sales order response
     */
    SalesOrderResponse cancelSalesOrder(Long id, String reason);
}
