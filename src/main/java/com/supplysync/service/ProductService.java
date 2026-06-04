package com.supplysync.service;

import com.supplysync.dto.request.ProductRequest;
import com.supplysync.dto.request.ProductSearchRequest;
import com.supplysync.dto.response.ProductDetailResponse;
import com.supplysync.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    /**
     * Creates a new product.
     * @param request the product details
     * @return the created product response
     */
    ProductResponse createProduct(ProductRequest request);

    /**
     * Searches products using dynamic specifications and pagination.
     * @param searchRequest filters
     * @param pageable pagination details
     * @return a page of product responses
     */
    Page<ProductResponse> searchProducts(ProductSearchRequest searchRequest, Pageable pageable);

    /**
     * Retrieves product detailed information including current inventory by warehouse.
     * @param id the product ID
     * @return the detailed product response
     */
    ProductDetailResponse getProductById(Long id);

    /**
     * Updates product details.
     * @param id the product ID
     * @param request the new product details
     * @return the updated product response
     */
    ProductResponse updateProduct(Long id, ProductRequest request);

    /**
     * Soft deletes a product.
     * @param id the product ID
     */
    void deleteProduct(Long id);
}
