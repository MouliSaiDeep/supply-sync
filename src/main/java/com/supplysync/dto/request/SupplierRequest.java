package com.supplysync.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class SupplierRequest {

    @Size(max = 20, message = "Supplier code must not exceed 20 characters")
    @Schema(description = "The unique code of the supplier", example = "SUP-TOPELEC-01")
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    @Size(max = 200, message = "Supplier name must not exceed 200 characters")
    @Schema(description = "The name of the supplier", requiredMode = Schema.RequiredMode.REQUIRED, example = "Top Electronics Supplies")
    private String name;

    @NotBlank(message = "Contact person is required")
    @Size(max = 150, message = "Contact person must not exceed 150 characters")
    @Schema(description = "The contact person name at the supplier company", requiredMode = Schema.RequiredMode.REQUIRED, example = "Sarah Connor")
    private String contactPerson;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(description = "The email address of the supplier", requiredMode = Schema.RequiredMode.REQUIRED, example = "sarah@topelec.com")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Schema(description = "The phone number of the supplier", requiredMode = Schema.RequiredMode.REQUIRED, example = "+1234567890")
    private String phone;

    @NotBlank(message = "Address is required")
    @Schema(description = "The primary address of the supplier", requiredMode = Schema.RequiredMode.REQUIRED, example = "100 Silicon Blvd")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "The city of the supplier address", requiredMode = Schema.RequiredMode.REQUIRED, example = "San Jose")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    @Schema(description = "The state of the supplier address", requiredMode = Schema.RequiredMode.REQUIRED, example = "CA")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Size(max = 10, message = "Pincode must not exceed 10 characters")
    @Schema(description = "The pin/postal code of the supplier address", requiredMode = Schema.RequiredMode.REQUIRED, example = "95112")
    private String pincode;

    @Size(max = 20, message = "GSTIN must not exceed 20 characters")
    @Schema(description = "The Tax Identification Number (GSTIN/VAT) of the supplier", example = "GSTIN12345678")
    private String gstin;

    @Schema(description = "Whether the supplier is active", example = "true")
    private Boolean isActive = true;
}
