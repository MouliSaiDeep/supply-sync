package com.supplysync.service;

import com.supplysync.dto.request.PurchaseOrderReceiptRequest;
import com.supplysync.dto.request.PurchaseOrderRequest;
import com.supplysync.dto.response.PurchaseOrderResponse;

public interface PurchaseOrderService {
    /**
     * Creates a new purchase order in DRAFT status.
     * @param request PO details
     * @return created PO response
     */
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request);

    /**
     * Submits a DRAFT purchase order for approval.
     * @param id PO ID
     * @return updated PO response
     */
    PurchaseOrderResponse submitPurchaseOrder(Long id);

    /**
     * Approves a PENDING_APPROVAL purchase order. Checks self-approval.
     * @param id PO ID
     * @param approvedByUserId approver user ID
     * @return updated PO response
     */
    PurchaseOrderResponse approvePurchaseOrder(Long id, Long approvedByUserId);

    /**
     * Records receipt of items for an approved/ordered purchase order.
     * @param id PO ID
     * @param request receipt details
     * @param performedByUserId receiver user ID
     * @return updated PO response
     */
    PurchaseOrderResponse receivePurchaseOrder(Long id, PurchaseOrderReceiptRequest request, Long performedByUserId);

    /**
     * Cancels a purchase order.
     * @param id PO ID
     * @param reason cancellation reason
     * @return updated PO response
     */
    PurchaseOrderResponse cancelPurchaseOrder(Long id, String reason);
}
