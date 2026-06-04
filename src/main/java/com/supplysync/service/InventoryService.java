package com.supplysync.service;

import com.supplysync.dto.request.InventoryAdjustRequest;
import com.supplysync.dto.request.InventoryTransferRequest;
import com.supplysync.dto.response.InventorySnapshotResponse;
import com.supplysync.dto.response.InventoryTransactionResponse;
import com.supplysync.dto.response.InventoryTransferResponse;
import com.supplysync.dto.response.LowStockAlertResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {
    /**
     * Performs an inventory adjustment (Inbound, Outbound, Damage report, etc.).
     * @param request the adjustment parameters
     * @param performedByUserId the ID of the user performing the adjustment
     * @return the logged inventory transaction details
     */
    InventoryTransactionResponse adjustInventory(InventoryAdjustRequest request, Long performedByUserId);

    /**
     * Performs a stock transfer between warehouses with pessimistic write locking.
     * @param request transfer details
     * @param performedByUserId user ID
     * @return the transfer response details
     */
    InventoryTransferResponse transferInventory(InventoryTransferRequest request, Long performedByUserId);

    /**
     * Retrieves products that are below or equal to their reorder levels. Caches result in Redis.
     * @return list of low stock items
     */
    List<LowStockAlertResponse> getLowStockAlerts();

    /**
     * Returns a paginated inventory snapshot for a specific warehouse.
     * @param warehouseId warehouse ID
     * @param pageable pagination details
     * @return a page of inventory snapshots
     */
    Page<InventorySnapshotResponse> getWarehouseInventory(Long warehouseId, Pageable pageable);
}
