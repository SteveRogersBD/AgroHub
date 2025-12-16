package com.socialmedia.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User login request")
public class LoginRequest {

    @NotBlank(message = "Email or username is required")
    @Schema(description = "User email or username", example = "user@example.com", required = true)
    private String emailOrUsername;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "SecurePass123!", required = true)
    private String password;
}
