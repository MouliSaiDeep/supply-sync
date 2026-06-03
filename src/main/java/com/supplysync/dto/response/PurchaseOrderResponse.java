package com.supplysync.dto.response;

import com.supplysync.enums.PurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {

    @Schema(description = "The database ID of the purchase order", example = "8")
    private Long id;

    @Schema(description = "The unique purchase order number", example = "PO-1780730005")
    private String poNumber;

    @Schema(description = "The database ID of the supplier", example = "4")
    private Long supplierId;

    @Schema(description = "The name of the supplier", example = "Global Tech Displays Inc")
    private String supplierName;

    @Schema(description = "The database ID of the warehouse", example = "1")
    private Long warehouseId;

    @Schema(description = "The name of the warehouse", example = "Midwest Distribution Hub")
    private String warehouseName;

    @Schema(description = "The current status of the purchase order", example = "DRAFT")
    private PurchaseOrderStatus status;

    @Schema(description = "The total cumulative order cost", example = "26000.00")
    private BigDecimal totalAmount;

    @Schema(description = "The expected delivery date", example = "2026-08-01")
    private LocalDate expectedDeliveryDate;

    @Schema(description = "The actual delivery receipt date", example = "2026-06-06")
    private LocalDate actualDeliveryDate;

    @Schema(description = "The ID of the user who created the PO", example = "2")
    private Long createdByUserId;

    @Schema(description = "The username of the creator", example = "proc_mgr_john")
    private String createdByUsername;

    @Schema(description = "The ID of the user who approved the PO", example = "1")
    private Long approvedByUserId;

    @Schema(description = "The username of the approver", example = "admin_john")
    private String approvedByUsername;

    @Schema(description = "Any comments or remarks", example = "Deliver after 9 AM only")
    private String notes;

    @Builder.Default
    @Schema(description = "The list of items and quantities ordered")
    private List<PurchaseOrderItemResponse> items = new ArrayList<>();

    @Schema(description = "The creation timestamp", example = "2026-06-06T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "The last updated timestamp", example = "2026-06-06T12:50:00")
    private LocalDateTime updatedAt;
}
