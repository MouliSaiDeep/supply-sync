package com.supplysync.repository;

import com.supplysync.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.warehouse.id = :warehouseId")
    Optional<Inventory> findWithLockByProductIdAndWarehouseId(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    Page<Inventory> findByWarehouseId(Long warehouseId, Pageable pageable);
    List<Inventory> findByWarehouseId(Long warehouseId);

    List<Inventory> findByProductId(Long productId);

    @Query("SELECT i FROM Inventory i " +
           "JOIN FETCH i.product p " +
           "JOIN FETCH i.warehouse w " +
           "WHERE i.quantityAvailable <= p.reorderLevel")
    List<Inventory> findLowStockInventory();

    @Query("SELECT COALESCE(SUM(i.quantityAvailable + i.quantityReserved + i.quantityDamaged), 0) FROM Inventory i WHERE i.warehouse.id = :warehouseId")
    long sumQuantityByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT COUNT(DISTINCT i.product.id) FROM Inventory i WHERE i.warehouse.id = :warehouseId")
    long countDistinctProductsByWarehouseId(@Param("warehouseId") Long warehouseId);

    boolean existsByWarehouseId(Long warehouseId);
}
