package com.supplysync.service.impl;

import com.supplysync.dto.request.WarehouseRequest;
import com.supplysync.dto.response.WarehouseDetailResponse;
import com.supplysync.dto.response.WarehouseResponse;
import com.supplysync.entity.Warehouse;
import com.supplysync.exception.DuplicateResourceException;
import com.supplysync.exception.ResourceNotFoundException;
import com.supplysync.util.Constants;
import com.supplysync.mapper.EntityMapper;
import com.supplysync.repository.InventoryRepository;
import com.supplysync.repository.WarehouseRepository;
import com.supplysync.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    @CacheEvict(value = "warehouses", allEntries = true)
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        log.info("Creating warehouse: {}", request.getName());

        String code = request.getWarehouseCode();
        if (code == null || code.trim().isEmpty()) {
            do {
                code = "WH-" + RandomStringUtils.randomAlphanumeric(6).toUpperCase();
            } while (warehouseRepository.existsByWarehouseCode(code));
        } else {
            if (warehouseRepository.existsByWarehouseCode(code)) {
                log.warn("Warehouse code already exists: {}", code);
                throw new DuplicateResourceException("RESOURCE_CONFLICT", "Warehouse code already exists: " + code);
            }
        }

        Warehouse warehouse = Warehouse.builder()
                .warehouseCode(code)
                .name(request.getName())
                .location(request.getLocation())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .capacity(request.getCapacity())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isDeleted(false)
                .build();

        Warehouse saved = warehouseRepository.save(warehouse);
        log.info("Warehouse created successfully with code: {}", saved.getWarehouseCode());
        return EntityMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseResponse> getAllWarehouses(int page, int size, String city, String state) {
        log.info("Retrieving all warehouses - page: {}, size: {}, city: {}, state: {}", page, size, city, state);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Warehouse> warehouses = warehouseRepository.findByCityAndState(city, state, pageable);
        return warehouses.map(EntityMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "warehouses", key = "#id")
    public WarehouseDetailResponse getWarehouseById(Long id) {
        log.info("Retrieving warehouse detail for ID: {}", id);
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));

        long totalProducts = inventoryRepository.countDistinctProductsByWarehouseId(id);
        long totalQuantity = inventoryRepository.sumQuantityByWarehouseId(id);

        return EntityMapper.toDetailResponse(warehouse, totalProducts, totalQuantity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "warehouses", allEntries = true)
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        log.info("Updating warehouse with ID: {}", id);
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));

        // Note: warehouseCode cannot be changed.
        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        warehouse.setCity(request.getCity());
        warehouse.setState(request.getState());
        warehouse.setPincode(request.getPincode());
        warehouse.setCapacity(request.getCapacity());
        if (request.getIsActive() != null) {
            warehouse.setIsActive(request.getIsActive());
        }

        Warehouse updated = warehouseRepository.save(warehouse);
        log.info("Warehouse updated successfully: {}", updated.getWarehouseCode());
        return EntityMapper.toResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "warehouses", allEntries = true)
    public void deleteWarehouse(Long id) {
        log.info("Soft deleting warehouse with ID: {}", id);
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));

        long totalQuantity = inventoryRepository.sumQuantityByWarehouseId(id);
        if (totalQuantity > 0) {
            log.warn("Cannot delete warehouse {} because it has active inventory of {} units", id, totalQuantity);
            throw new DuplicateResourceException(Constants.ERR_WAREHOUSE_ACTIVE_INVENTORY, "Warehouse has active inventory and cannot be deleted");
        }

        warehouse.setIsDeleted(true);
        warehouseRepository.save(warehouse);
        log.info("Warehouse soft deleted successfully with ID: {}", id);
    }
}
