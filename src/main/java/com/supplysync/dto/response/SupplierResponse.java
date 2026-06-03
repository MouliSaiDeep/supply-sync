package com.supplysync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {

    @Schema(description = "The database ID of the supplier", example = "4")
    private Long id;

    @Schema(description = "The unique code of the supplier", example = "SUP-GLB-DISP")
    private String supplierCode;

    @Schema(description = "The name of the supplier", example = "Global Tech Displays Inc")
    private String name;

    @Schema(description = "The contact person at the supplier", example = "Sarah Connor")
    private String contactPerson;

    @Schema(description = "The email address of the supplier", example = "sarah.connor@globaldisplays.com")
    private String email;

    @Schema(description = "The phone number of the supplier", example = "+1-312-555-0199")
    private String phone;

    @Schema(description = "The address of the supplier", example = "900 Industrial Pkwy")
    private String address;

    @Schema(description = "The city of the supplier address", example = "Chicago")
    private String city;

    @Schema(description = "The state of the supplier address", example = "IL")
    private String state;

    @Schema(description = "The postal code of the supplier address", example = "60609")
    private String pincode;

    @Schema(description = "The Tax Identification Number (GSTIN/VAT) of the supplier", example = "GSTIN12345678")
    private String gstin;

    @Schema(description = "Whether the supplier is active", example = "true")
    private Boolean isActive;

    @Schema(description = "The creation timestamp", example = "2026-06-06T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "The last updated timestamp", example = "2026-06-06T12:50:00")
    private LocalDateTime updatedAt;
}
