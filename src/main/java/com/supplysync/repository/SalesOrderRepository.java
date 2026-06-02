package com.supplysync.repository;

import com.supplysync.entity.SalesOrder;
import com.supplysync.enums.SalesOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    Optional<SalesOrder> findByOrderNumber(String orderNumber);
    boolean existsByOrderNumber(String orderNumber);

    long countByStatus(SalesOrderStatus status);

    @Query("SELECT so FROM SalesOrder so WHERE " +
           "so.createdAt >= :startDate AND " +
           "so.createdAt <= :endDate AND " +
           "(:warehouseId IS NULL OR so.warehouse.id = :warehouseId) AND " +
           "(:status IS NULL OR so.status = :status)")
    List<SalesOrder> findForSummaryReport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("warehouseId") Long warehouseId,
            @Param("status") SalesOrderStatus status
    );
}
