package com.supplysync.controller;

import com.supplysync.dto.request.WarehouseRequest;
import com.supplysync.dto.response.WarehouseDetailResponse;
import com.supplysync.dto.response.WarehouseResponse;
import com.supplysync.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouse Management", description = "Endpoints for creating, updating, viewing, and deleting warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new warehouse", description = "Allows ADMIN to create a new warehouse. Generates warehouse_code if not provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Warehouse created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Warehouse code already exists")
    })
    public ResponseEntity<WarehouseResponse> createWarehouse(@Valid @RequestBody WarehouseRequest request) {
        WarehouseResponse response = warehouseService.createWarehouse(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get paginated list of warehouses", description = "Returns a paginated list of active warehouses with optional city and state filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of warehouses retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<WarehouseResponse>> getAllWarehouses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state
    ) {
        Page<WarehouseResponse> response = warehouseService.getAllWarehouses(page, size, city, state);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get warehouse by ID", description = "Returns detailed information of a single warehouse including inventory summary.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Warehouse details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<WarehouseDetailResponse> getWarehouseById(@PathVariable Long id) {
        WarehouseDetailResponse response = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update warehouse details", description = "Allows ADMIN to update warehouse attributes. warehouse_code cannot be changed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Warehouse updated successfully"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<WarehouseResponse> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequest request
    ) {
        WarehouseResponse response = warehouseService.updateWarehouse(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete warehouse", description = "Allows ADMIN to soft delete a warehouse. Fails if the warehouse contains non-zero inventory quantities.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Warehouse deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Warehouse has active non-zero inventory")
    })
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok().build();
    }
}
