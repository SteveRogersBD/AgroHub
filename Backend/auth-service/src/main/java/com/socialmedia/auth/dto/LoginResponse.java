package com.socialmedia.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Login response with JWT tokens and user information")
public class LoginResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "User ID", example = "1")
    private Long userId;
    
    @Schema(description = "User email", example = "user@example.com")
    private String email;
    
    @Schema(description = "Username", example = "johndoe")
    private String username;
    
    @Schema(description = "User role", example = "USER")
    private String role;
}
