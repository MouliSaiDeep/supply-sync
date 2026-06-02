package com.supplysync.repository;

import com.supplysync.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByWarehouseCode(String warehouseCode);
    boolean existsByWarehouseCode(String warehouseCode);

    @Query("SELECT w FROM Warehouse w WHERE " +
           "(:city IS NULL OR :city = '' OR LOWER(w.city) = LOWER(:city)) AND " +
           "(:state IS NULL OR :state = '' OR LOWER(w.state) = LOWER(:state))")
    Page<Warehouse> findByCityAndState(@Param("city") String city, @Param("state") String state, Pageable pageable);
}
