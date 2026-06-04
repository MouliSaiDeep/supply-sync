package com.supplysync.service;

import com.supplysync.dto.request.CategoryRequest;
import com.supplysync.dto.response.CategoryResponse;
import com.supplysync.dto.response.CategoryTreeResponse;

import java.util.List;

public interface CategoryService {
    /**
     * Creates a new category.
     * @param request the category details
     * @return the created category response
     */
    CategoryResponse createCategory(CategoryRequest request);

    /**
     * Retrieves the category tree hierarchy built in memory to avoid N+1 queries.
     * @return a list of root category nodes in the tree
     */
    List<CategoryTreeResponse> getCategoryTree();
}
