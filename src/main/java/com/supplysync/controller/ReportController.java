package com.supplysync.controller;

import com.supplysync.dto.response.DashboardResponse;
import com.supplysync.dto.response.InventoryValuationResponse;
import com.supplysync.dto.response.PurchaseOrderSummaryResponse;
import com.supplysync.dto.response.SalesOrderSummaryResponse;
import com.supplysync.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reporting & Analytics", description = "Endpoints for retrieving business reports and dashboard summaries")
@PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PROCUREMENT_MANAGER')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard summary metrics", description = "Allows ADMIN, WAREHOUSE_MANAGER, or PROCUREMENT_MANAGER to view key dashboard metrics, cached in Redis for 10 minutes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard summary retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<DashboardResponse> getDashboardSummary() {
        DashboardResponse response = reportService.getDashboardSummary();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inventory-valuation")
    @Operation(summary = "Get inventory valuation report", description = "Calculates total asset value of stored inventory globally or for a specific warehouse.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory valuation calculated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<InventoryValuationResponse> getInventoryValuation(
            @RequestParam(required = false) Long warehouseId
    ) {
        InventoryValuationResponse response = reportService.getInventoryValuation(warehouseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/purchase-orders/summary")
    @Operation(summary = "Get PO summary report", description = "Aggregates purchase orders, breakdown by status, and lists top suppliers by purchase value in a period.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase order summary report retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range parameters"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<PurchaseOrderSummaryResponse> getPurchaseOrderSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status
    ) {
        PurchaseOrderSummaryResponse response = reportService.getPurchaseOrderSummary(startDate, endDate, supplierId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sales-orders/summary")
    @Operation(summary = "Get sales order summary report", description = "Aggregates revenue, AOV, order status distributions, and top revenue generating products in a period.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sales order summary report retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date parameters"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<SalesOrderSummaryResponse> getSalesOrderSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String status
    ) {
        SalesOrderSummaryResponse response = reportService.getSalesOrderSummary(startDate, endDate, warehouseId, status);
        return ResponseEntity.ok(response);
    }
}
