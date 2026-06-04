package com.supplysync.service;

import com.supplysync.dto.request.WarehouseRequest;
import com.supplysync.dto.response.WarehouseDetailResponse;
import com.supplysync.dto.response.WarehouseResponse;
import org.springframework.data.domain.Page;

public interface WarehouseService {
    /**
     * Creates a new warehouse.
     * @param request the warehouse details
     * @return the created warehouse response
     */
    WarehouseResponse createWarehouse(WarehouseRequest request);

    /**
     * Retrieves all active warehouses with pagination and optional city/state filters.
     * @param page page number
     * @param size page size
     * @param city optional city filter
     * @param state optional state filter
     * @return a page of warehouse responses
     */
    Page<WarehouseResponse> getAllWarehouses(int page, int size, String city, String state);

    /**
     * Retrieves a single warehouse by ID including inventory summaries.
     * @param id the warehouse ID
     * @return the detailed warehouse response
     */
    WarehouseDetailResponse getWarehouseById(Long id);

    /**
     * Updates an existing warehouse details.
     * @param id the warehouse ID
     * @param request the updated warehouse details
     * @return the updated warehouse response
     */
    WarehouseResponse updateWarehouse(Long id, WarehouseRequest request);

    /**
     * Soft deletes a warehouse.
     * @param id the warehouse ID
     */
    void deleteWarehouse(Long id);
}
