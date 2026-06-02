package com.supplysync.repository;

import com.supplysync.entity.SalesOrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {

    @Query("SELECT soi.product.id, soi.product.sku, soi.product.name, SUM(soi.quantity) as totalQty " +
           "FROM SalesOrderItem soi JOIN soi.salesOrder so " +
           "WHERE so.status IN (com.supplysync.enums.SalesOrderStatus.DISPATCHED, com.supplysync.enums.SalesOrderStatus.DELIVERED) " +
           "AND so.dispatchedAt >= :sinceDate " +
           "GROUP BY soi.product.id, soi.product.sku, soi.product.name " +
           "ORDER BY SUM(soi.quantity) DESC")
    List<Object[]> findTopSellingProducts(@Param("sinceDate") LocalDateTime sinceDate, Pageable pageable);
}
