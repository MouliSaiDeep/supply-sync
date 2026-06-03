package com.supplysync.dto.response;

import com.supplysync.enums.SalesOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderResponse {

    @Schema(description = "The database ID of the sales order", example = "22")
    private Long id;

    @Schema(description = "The unique sales order number", example = "SO-1780730412")
    private String orderNumber;

    @Schema(description = "The name of the customer", example = "Acme Retailers")
    private String customerName;

    @Schema(description = "The email address of the customer", example = "billing@acme.com")
    private String customerEmail;

    @Schema(description = "The contact phone of the customer", example = "+1-555-123-4567")
    private String customerPhone;

    @Schema(description = "The shipping address for the order", example = "123 ACME Way, Suite 100")
    private String shippingAddress;

    @Schema(description = "The database ID of the dispatching warehouse", example = "1")
    private Long warehouseId;

    @Schema(description = "The name of the warehouse", example = "Midwest Distribution Hub")
    private String warehouseName;

    @Schema(description = "The current status of the sales order", example = "CONFIRMED")
    private SalesOrderStatus status;

    @Schema(description = "The total cumulative order value", example = "4999.90")
    private BigDecimal totalAmount;

    @Schema(description = "The dispatch timestamp", example = "2026-06-06T12:51:30")
    private LocalDateTime dispatchedAt;

    @Schema(description = "The delivery timestamp", example = "2026-06-06T13:00:00")
    private LocalDateTime deliveredAt;

    @Schema(description = "The ID of the user who created the sales order", example = "3")
    private Long createdByUserId;

    @Schema(description = "The username of the creator", example = "bob_staff")
    private String createdByUsername;

    @Schema(description = "Optional notes or instructions", example = "Leave at loading dock 4")
    private String notes;

    @Builder.Default
    @Schema(description = "The list of ordered items and quantities")
    private List<SalesOrderItemResponse> items = new ArrayList<>();

    @Schema(description = "The creation timestamp", example = "2026-06-06T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "The last updated timestamp", example = "2026-06-06T12:50:00")
    private LocalDateTime updatedAt;
}
