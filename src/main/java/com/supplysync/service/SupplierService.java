package com.supplysync.service;

import com.supplysync.dto.request.SupplierRequest;
import com.supplysync.dto.response.SupplierResponse;
import org.springframework.data.domain.Page;

public interface SupplierService {
    /**
     * Creates a new supplier.
     * @param request the supplier details
     * @return the created supplier response
     */
    SupplierResponse createSupplier(SupplierRequest request);

    /**
     * Updates an existing supplier.
     * @param id the supplier ID
     * @param request the updated details
     * @return the updated supplier response
     */
    SupplierResponse updateSupplier(Long id, SupplierRequest request);

    /**
     * Retrieves a supplier by ID.
     * @param id the supplier ID
     * @return the supplier response
     */
    SupplierResponse getSupplierById(Long id);

    /**
     * Retrieves all suppliers with pagination and optional city/state filters.
     * @param page page number
     * @param size page size
     * @param city optional city filter
     * @param state optional state filter
     * @return a page of supplier responses
     */
    Page<SupplierResponse> getAllSuppliers(int page, int size, String city, String state);

    /**
     * Soft deletes a supplier.
     * @param id the supplier ID
     */
    void deleteSupplier(Long id);
}
