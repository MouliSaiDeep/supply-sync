package com.supplysync.service.impl;

import com.supplysync.dto.request.SupplierRequest;
import com.supplysync.dto.response.SupplierResponse;
import com.supplysync.entity.Supplier;
import com.supplysync.exception.DuplicateResourceException;
import com.supplysync.exception.ResourceNotFoundException;
import com.supplysync.mapper.EntityMapper;
import com.supplysync.repository.SupplierRepository;
import com.supplysync.service.SupplierService;
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
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    @Transactional
    @CacheEvict(value = "suppliers", allEntries = true)
    public SupplierResponse createSupplier(SupplierRequest request) {
        log.info("Creating supplier: {}", request.getName());

        String code = request.getSupplierCode();
        if (code == null || code.trim().isEmpty()) {
            do {
                code = "SUP-" + RandomStringUtils.randomAlphanumeric(6).toUpperCase();
            } while (supplierRepository.existsBySupplierCode(code));
        } else {
            if (supplierRepository.existsBySupplierCode(code)) {
                log.warn("Supplier code already exists: {}", code);
                throw new DuplicateResourceException("RESOURCE_CONFLICT", "Supplier code already exists: " + code);
            }
        }

        Supplier supplier = Supplier.builder()
                .supplierCode(code)
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .gstin(request.getGstin())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isDeleted(false)
                .build();

        Supplier saved = supplierRepository.save(supplier);
        log.info("Supplier created successfully with code: {}", saved.getSupplierCode());
        return EntityMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "suppliers", allEntries = true)
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        log.info("Updating supplier with ID: {}", id);
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));

        // Note: supplierCode cannot be changed.
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setPincode(request.getPincode());
        supplier.setGstin(request.getGstin());
        if (request.getIsActive() != null) {
            supplier.setIsActive(request.getIsActive());
        }

        Supplier updated = supplierRepository.save(supplier);
        log.info("Supplier updated successfully with code: {}", updated.getSupplierCode());
        return EntityMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "suppliers", key = "#id")
    public SupplierResponse getSupplierById(Long id) {
        log.info("Retrieving supplier with ID: {}", id);
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));
        return EntityMapper.toResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAllSuppliers(int page, int size, String city, String state) {
        log.info("Retrieving all suppliers - page: {}, size: {}, city: {}, state: {}", page, size, city, state);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Supplier> suppliers = supplierRepository.findByCityAndState(city, state, pageable);
        return suppliers.map(EntityMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "suppliers", allEntries = true)
    public void deleteSupplier(Long id) {
        log.info("Soft deleting supplier with ID: {}", id);
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));

        supplier.setIsDeleted(true);
        supplierRepository.save(supplier);
        log.info("Supplier soft deleted successfully with ID: {}", id);
    }
}
