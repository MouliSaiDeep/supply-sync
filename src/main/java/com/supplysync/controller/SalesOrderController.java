package com.supplysync.controller;

import com.supplysync.dto.request.SalesOrderRequest;
import com.supplysync.dto.response.SalesOrderResponse;
import com.supplysync.entity.User;
import com.supplysync.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales-orders")
@RequiredArgsConstructor
@Tag(name = "Sales Order Management", description = "Endpoints for creating, dispatching, delivering, and cancelling sales orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Create sales order", description = "Allows STAFF or WAREHOUSE_MANAGER to create a sales order. Performs immediate stock verification and reservation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "210", description = "Sales order created successfully in CONFIRMED status"),
            @ApiResponse(responseCode = "400", description = "Invalid input details"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "422", description = "Insufficient inventory stock for order")
    })
    public ResponseEntity<SalesOrderResponse> createSalesOrder(
            @Valid @RequestBody SalesOrderRequest request,
            @AuthenticationPrincipal User user
    ) {
        SalesOrderResponse response = salesOrderService.createSalesOrder(request, user.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/dispatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'STAFF')")
    @Operation(summary = "Dispatch sales order", description = "Fulfills reserved stock, logs outbound inventory transactions, and transitions order status to DISPATCHED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order dispatched successfully"),
            @ApiResponse(responseCode = "404", description = "Sales order not found"),
            @ApiResponse(responseCode = "422", description = "Order not confirmed or invalid inventory details")
    })
    public ResponseEntity<SalesOrderResponse> dispatchSalesOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        SalesOrderResponse response = salesOrderService.dispatchSalesOrder(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'STAFF')")
    @Operation(summary = "Deliver sales order", description = "Transitions order status to DELIVERED and registers delivery timestamp.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order delivered successfully"),
            @ApiResponse(responseCode = "422", description = "Order must be in DISPATCHED status")
    })
    public ResponseEntity<SalesOrderResponse> deliverSalesOrder(@PathVariable Long id) {
        SalesOrderResponse response = salesOrderService.deliverSalesOrder(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'STAFF')")
    @Operation(summary = "Cancel sales order", description = "Cancels a confirmed order, releases reserved inventory quantities, and triggers a cancellation event.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "422", description = "Order cannot be cancelled in current status")
    })
    public ResponseEntity<SalesOrderResponse> cancelSalesOrder(
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        SalesOrderResponse response = salesOrderService.cancelSalesOrder(id, reason);
        return ResponseEntity.ok(response);
    }
}
