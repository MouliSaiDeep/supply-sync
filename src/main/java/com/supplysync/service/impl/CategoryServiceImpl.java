package com.supplysync.service.impl;

import com.supplysync.dto.request.CategoryRequest;
import com.supplysync.dto.response.CategoryResponse;
import com.supplysync.dto.response.CategoryTreeResponse;
import com.supplysync.entity.Category;
import com.supplysync.exception.DuplicateResourceException;
import com.supplysync.exception.ResourceNotFoundException;
import com.supplysync.mapper.EntityMapper;
import com.supplysync.repository.CategoryRepository;
import com.supplysync.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating category: {}", request.getName());

        String code = request.getCategoryCode();
        if (code == null || code.trim().isEmpty()) {
            do {
                code = "CAT-" + RandomStringUtils.randomAlphanumeric(6).toUpperCase();
            } while (categoryRepository.existsByCategoryCode(code));
        } else {
            if (categoryRepository.existsByCategoryCode(code)) {
                log.warn("Category code already exists: {}", code);
                throw new DuplicateResourceException("RESOURCE_CONFLICT", "Category code already exists: " + code);
            }
        }

        Category parent = null;
        if (request.getParentCategoryId() != null) {
            parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentCategoryId()));
        }

        Category category = Category.builder()
                .categoryCode(code)
                .name(request.getName())
                .description(request.getDescription())
                .parentCategory(parent)
                .isDeleted(false)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with code: {}", saved.getCategoryCode());
        return EntityMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'tree'")
    public List<CategoryTreeResponse> getCategoryTree() {
        log.info("Building category tree in memory");
        // Fetch all categories (only non-deleted due to @SQLRestriction)
        List<Category> allCategories = categoryRepository.findAll();

        // Step 1: Map all categories to CategoryTreeResponse and store in a map
        Map<Long, CategoryTreeResponse> nodeMap = new HashMap<>();
        for (Category cat : allCategories) {
            CategoryTreeResponse node = CategoryTreeResponse.builder()
                    .id(cat.getId())
                    .categoryCode(cat.getCategoryCode())
                    .name(cat.getName())
                    .description(cat.getDescription())
                    .children(new ArrayList<>())
                    .build();
            nodeMap.put(cat.getId(), node);
        }

        // Step 2: Assemble the hierarchy
        List<CategoryTreeResponse> rootCategories = new ArrayList<>();
        for (Category cat : allCategories) {
            CategoryTreeResponse currentResponseNode = nodeMap.get(cat.getId());
            if (cat.getParentCategory() != null) {
                CategoryTreeResponse parentResponseNode = nodeMap.get(cat.getParentCategory().getId());
                if (parentResponseNode != null) {
                    parentResponseNode.getChildren().add(currentResponseNode);
                } else {
                    // Fallback: if parent is deleted/not found in map, treat as root
                    rootCategories.add(currentResponseNode);
                }
            } else {
                rootCategories.add(currentResponseNode);
            }
        }

        log.info("Category tree assembled with {} root categories", rootCategories.size());
        return rootCategories;
    }
}
