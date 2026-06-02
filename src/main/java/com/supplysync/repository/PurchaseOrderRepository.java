package com.supplysync.repository;

import com.supplysync.entity.PurchaseOrder;
import com.supplysync.enums.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByPoNumber(String poNumber);
    boolean existsByPoNumber(String poNumber);

    long countByStatus(PurchaseOrderStatus status);

    @Query("SELECT po FROM PurchaseOrder po WHERE " +
           "po.createdAt >= :startDate AND " +
           "po.createdAt <= :endDate AND " +
           "(:supplierId IS NULL OR po.supplier.id = :supplierId) AND " +
           "(:status IS NULL OR po.status = :status)")
    List<PurchaseOrder> findForSummaryReport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("supplierId") Long supplierId,
            @Param("status") PurchaseOrderStatus status
    );
}
