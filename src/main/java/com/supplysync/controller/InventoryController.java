package com.supplysync.controller;

import com.supplysync.dto.request.InventoryAdjustRequest;
import com.supplysync.dto.request.InventoryTransferRequest;
import com.supplysync.dto.response.InventorySnapshotResponse;
import com.supplysync.dto.response.InventoryTransactionResponse;
import com.supplysync.dto.response.InventoryTransferResponse;
import com.supplysync.dto.response.LowStockAlertResponse;
import com.supplysync.entity.User;
import com.supplysync.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "Endpoints for inventory adjustments, transfers, and stock alerts")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'STAFF')")
    @Operation(summary = "Adjust inventory levels", description = "Allows WAREHOUSE_MANAGER or STAFF to log inbound/outbound adjustments or damage reports.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adjustment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input details"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "422", description = "Insufficient inventory or invalid transaction type")
    })
    public ResponseEntity<InventoryTransactionResponse> adjustInventory(
            @Valid @RequestBody InventoryAdjustRequest request,
            @AuthenticationPrincipal User user
    ) {
        InventoryTransactionResponse response = inventoryService.adjustInventory(request, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'STAFF')")
    @Operation(summary = "Transfer inventory between warehouses", description = "Allows WAREHOUSE_MANAGER or STAFF to atomically transfer stock using pessimistic write locks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input details"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "422", description = "Insufficient stock at source warehouse")
    })
    public ResponseEntity<InventoryTransferResponse> transferInventory(
            @Valid @RequestBody InventoryTransferRequest request,
            @AuthenticationPrincipal User user
    ) {
        InventoryTransferResponse response = inventoryService.transferInventory(request, user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'STAFF')")
    @Operation(summary = "Get low stock products", description = "Returns products whose available quantity has fallen below or equal to their reorder level. Caches response in Redis.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Low stock list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<LowStockAlertResponse>> getLowStockAlerts() {
        List<LowStockAlertResponse> alerts = inventoryService.getLowStockAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'STAFF')")
    @Operation(summary = "Get warehouse inventory snapshots", description = "Returns a paginated list of all products currently stored in a given warehouse.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Warehouse inventory page retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<InventorySnapshotResponse>> getWarehouseInventory(
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdatedAt").descending());
        Page<InventorySnapshotResponse> response = inventoryService.getWarehouseInventory(warehouseId, pageable);
        return ResponseEntity.ok(response);
    }
}
