package com.supplysync.repository;

import com.supplysync.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findBySupplierCode(String supplierCode);
    boolean existsBySupplierCode(String supplierCode);

    @Query("SELECT s FROM Supplier s WHERE " +
           "(:city IS NULL OR :city = '' OR LOWER(s.city) = LOWER(:city)) AND " +
           "(:state IS NULL OR :state = '' OR LOWER(s.state) = LOWER(:state))")
    Page<Supplier> findByCityAndState(@Param("city") String city, @Param("state") String state, Pageable pageable);
}
