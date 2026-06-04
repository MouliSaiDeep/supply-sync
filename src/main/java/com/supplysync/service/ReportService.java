package com.supplysync.service;

import com.supplysync.dto.response.DashboardResponse;
import com.supplysync.dto.response.InventoryValuationResponse;
import com.supplysync.dto.response.PurchaseOrderSummaryResponse;
import com.supplysync.dto.response.SalesOrderSummaryResponse;

import java.time.LocalDate;

public interface ReportService {
    /**
     * Retrieves dashboard summary metrics, cached in Redis for 10 minutes.
     * @return the dashboard summary details
     */
    DashboardResponse getDashboardSummary();

    /**
     * Calculates the total financial value of inventory globally or per warehouse.
     * @param warehouseId optional warehouse ID filter
     * @return the inventory valuation details
     */
    InventoryValuationResponse getInventoryValuation(Long warehouseId);

    /**
     * Retrieves purchase order aggregation metrics over a date period.
     * @param startDate start date
     * @param endDate end date
     * @param supplierId optional supplier filter
     * @param status optional status filter
     * @return purchase order summary details
     */
    PurchaseOrderSummaryResponse getPurchaseOrderSummary(LocalDate startDate, LocalDate endDate, Long supplierId, String status);

    /**
     * Retrieves sales order aggregation metrics over a date period.
     * @param startDate start date
     * @param endDate end date
     * @param warehouseId optional warehouse filter
     * @param status optional status filter
     * @return sales order summary details
     */
    SalesOrderSummaryResponse getSalesOrderSummary(LocalDate startDate, LocalDate endDate, Long warehouseId, String status);
}
