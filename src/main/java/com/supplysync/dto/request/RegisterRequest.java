package com.supplysync.dto.request;

import com.supplysync.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "The username for the new account", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin_user")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(description = "The email address for the new account", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin@supplysync.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "The password for the new account", requiredMode = Schema.RequiredMode.REQUIRED, example = "SecurePassword123")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    @Schema(description = "The full name of the user", requiredMode = Schema.RequiredMode.REQUIRED, example = "John Doe")
    private String fullName;

    @NotNull(message = "Role is required")
    @Schema(description = "The role of the new user", requiredMode = Schema.RequiredMode.REQUIRED, example = "ADMIN")
    private UserRole role;
}
