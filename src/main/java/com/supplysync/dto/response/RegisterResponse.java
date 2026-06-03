package com.supplysync.dto.response;

import com.supplysync.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    @Schema(description = "The database ID of the registered user", example = "1")
    private Long id;

    @Schema(description = "The username of the registered user", example = "admin_user")
    private String username;

    @Schema(description = "The email of the registered user", example = "admin@supplysync.com")
    private String email;

    @Schema(description = "The full name of the registered user", example = "John Doe")
    private String fullName;

    @Schema(description = "The assigned user role", example = "ADMIN")
    private UserRole role;

    @Schema(description = "The JWT access token", example = "eyJhbGciOi...")
    private String accessToken;

    @Schema(description = "The JWT refresh token", example = "48b6289b...")
    private String refreshToken;
}
