package com.supplysync.service.impl;

import com.supplysync.dto.request.ProductRequest;
import com.supplysync.dto.request.ProductSearchRequest;
import com.supplysync.dto.response.InventorySummary;
import com.supplysync.dto.response.ProductDetailResponse;
import com.supplysync.dto.response.ProductResponse;
import com.supplysync.entity.Category;
import com.supplysync.entity.Product;
import com.supplysync.entity.Inventory;
import com.supplysync.exception.DuplicateResourceException;
import com.supplysync.exception.ResourceNotFoundException;
import com.supplysync.mapper.EntityMapper;
import com.supplysync.repository.CategoryRepository;
import com.supplysync.repository.InventoryRepository;
import com.supplysync.repository.ProductRepository;
import com.supplysync.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product: {}", request.getName());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        String sku = request.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            do {
                sku = "SKU-" + category.getCategoryCode().toUpperCase() + "-" + RandomStringUtils.randomAlphanumeric(8).toUpperCase();
            } while (productRepository.existsBySku(sku));
        } else {
            if (productRepository.existsBySku(sku)) {
                log.warn("SKU already exists: {}", sku);
                throw new DuplicateResourceException("RESOURCE_CONFLICT", "SKU already exists: " + sku);
            }
        }

        Product product = Product.builder()
                .sku(sku)
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .unitPrice(request.getUnitPrice())
                .unitOfMeasure(request.getUnitOfMeasure())
                .reorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isDeleted(false)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created successfully with SKU: {}", saved.getSku());
        return EntityMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(ProductSearchRequest searchRequest, Pageable pageable) {
        log.info("Searching products with filters: {}", searchRequest);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchRequest.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), searchRequest.getCategoryId()));
            }

            if (searchRequest.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), searchRequest.getIsActive()));
            }

            if (searchRequest.getMinPrice() != null) {
                predicates.add(cb.ge(root.get("unitPrice"), searchRequest.getMinPrice()));
            }

            if (searchRequest.getMaxPrice() != null) {
                predicates.add(cb.le(root.get("unitPrice"), searchRequest.getMaxPrice()));
            }

            if (searchRequest.getSearch() != null && !searchRequest.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + searchRequest.getSearch().trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(nameLike, descLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(EntityMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductDetailResponse getProductById(Long id) {
        log.info("Retrieving product detail for ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        List<Inventory> inventories = inventoryRepository.findByProductId(id);
        List<InventorySummary> inventorySummaries = inventories.stream()
                .map(EntityMapper::toSummary)
                .collect(Collectors.toList());

        return EntityMapper.toDetailResponse(product, inventorySummaries);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        // Note: SKU cannot be changed.
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setUnitPrice(request.getUnitPrice());
        product.setUnitOfMeasure(request.getUnitOfMeasure());
        product.setReorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : 0);
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }

        Product updated = productRepository.save(product);
        log.info("Product updated successfully with SKU: {}", updated.getSku());
        return EntityMapper.toResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Soft deleting product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setIsDeleted(true);
        productRepository.save(product);
        log.info("Product soft deleted successfully with ID: {}", id);
    }
}
