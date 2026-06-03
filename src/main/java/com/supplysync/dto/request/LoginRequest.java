package com.supplysync.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "The username of the user", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin_user")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "The password of the user", requiredMode = Schema.RequiredMode.REQUIRED, example = "SecurePassword123")
    private String password;
}
