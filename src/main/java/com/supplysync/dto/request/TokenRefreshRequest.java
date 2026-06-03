package com.supplysync.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "The refresh token to generate a new access token", requiredMode = Schema.RequiredMode.REQUIRED, example = "48b6289b-871d-4eb2-a16f-631d8ce0a9f5")
    private String refreshToken;
}
