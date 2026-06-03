package com.supplysync.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
public class SalesOrderRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 200, message = "Customer name must not exceed 200 characters")
    @Schema(description = "The name of the customer placing the order", requiredMode = Schema.RequiredMode.REQUIRED, example = "Acme Retailers")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    @Size(max = 100, message = "Customer email must not exceed 100 characters")
    @Schema(description = "The email address of the customer", requiredMode = Schema.RequiredMode.REQUIRED, example = "billing@acme.com")
    private String customerEmail;

    @NotBlank(message = "Customer phone is required")
    @Size(max = 20, message = "Customer phone must not exceed 20 characters")
    @Schema(description = "The contact phone number of the customer", requiredMode = Schema.RequiredMode.REQUIRED, example = "+1-555-123-4567")
    private String customerPhone;

    @NotBlank(message = "Shipping address is required")
    @Schema(description = "The physical address to ship the ordered goods", requiredMode = Schema.RequiredMode.REQUIRED, example = "123 ACME Way, Suite 100")
    private String shippingAddress;

    @NotNull(message = "Warehouse ID is required")
    @Schema(description = "The database ID of the warehouse dispatching the items", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long warehouseId;

    @Schema(description = "Optional notes or delivery instructions", example = "Leave at loading dock 4")
    private String notes;

    @NotEmpty(message = "Sales order must contain at least one item")
    @Valid
    @Schema(description = "The list of products and quantities ordered", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SalesOrderItemRequest> items;
}
