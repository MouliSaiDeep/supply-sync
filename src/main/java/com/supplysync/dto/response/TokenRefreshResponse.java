package com.supplysync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {

    @Schema(description = "The new JWT access token", example = "eyJhbGciOi...")
    private String accessToken;

    @Schema(description = "The JWT refresh token", example = "48b6289b...")
    private String refreshToken;
}
