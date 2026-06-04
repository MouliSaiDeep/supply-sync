package com.supplysync.controller;

import com.supplysync.dto.request.PurchaseOrderReceiptRequest;
import com.supplysync.dto.request.PurchaseOrderRequest;
import com.supplysync.dto.response.PurchaseOrderResponse;
import com.supplysync.entity.User;
import com.supplysync.service.PurchaseOrderService;
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
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Order Management", description = "Endpoints for creating, submitting, approving, receiving, and cancelling purchase orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER')")
    @Operation(summary = "Create purchase order", description = "Allows PROCUREMENT_MANAGER to create a new purchase order in DRAFT status. PO number will be auto-generated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Purchase order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input details"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequest request) {
        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('PROCUREMENT_MANAGER')")
    @Operation(summary = "Submit purchase order", description = "Allows PROCUREMENT_MANAGER to submit a DRAFT purchase order for approval.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase order submitted successfully"),
            @ApiResponse(responseCode = "404", description = "Purchase order not found"),
            @ApiResponse(responseCode = "422", description = "PO must be draft and have items")
    })
    public ResponseEntity<PurchaseOrderResponse> submitPurchaseOrder(@PathVariable Long id) {
        PurchaseOrderResponse response = purchaseOrderService.submitPurchaseOrder(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Approve purchase order", description = "Allows ADMIN or WAREHOUSE_MANAGER to approve a purchase order. Creator cannot approve.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase order approved successfully"),
            @ApiResponse(responseCode = "403", description = "Self-approval not allowed or unauthorized role"),
            @ApiResponse(responseCode = "404", description = "Purchase order not found"),
            @ApiResponse(responseCode = "422", description = "PO not in pending approval status")
    })
    public ResponseEntity<PurchaseOrderResponse> approvePurchaseOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        PurchaseOrderResponse response = purchaseOrderService.approvePurchaseOrder(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'STAFF')")
    @Operation(summary = "Receive purchase order items", description = "Logs delivery of items for an approved purchase order and updates inventory levels.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Items received and inventory adjusted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid receipt details"),
            @ApiResponse(responseCode = "422", description = "PO not in approved/partially received status or quantity exceeds ordered")
    })
    public ResponseEntity<PurchaseOrderResponse> receivePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderReceiptRequest request,
            @AuthenticationPrincipal User user
    ) {
        PurchaseOrderResponse response = purchaseOrderService.receivePurchaseOrder(id, request, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER')")
    @Operation(summary = "Cancel purchase order", description = "Allows ADMIN or PROCUREMENT_MANAGER to cancel a purchase order if not received.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase order cancelled successfully"),
            @ApiResponse(responseCode = "422", description = "PO cancellation not allowed in current status")
    })
    public ResponseEntity<PurchaseOrderResponse> cancelPurchaseOrder(
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        PurchaseOrderResponse response = purchaseOrderService.cancelPurchaseOrder(id, reason);
        return ResponseEntity.ok(response);
    }
}
