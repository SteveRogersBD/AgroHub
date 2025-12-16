package com.socialmedia.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Token validation response")
public class TokenValidationResponse {

    @Schema(description = "Whether the token is valid", example = "true")
    private boolean valid;
    
    @Schema(description = "User ID from token", example = "1")
    private Long userId;
    
    @Schema(description = "User roles from token", example = "[\"USER\"]")
    private List<String> roles;
    
    @Schema(description = "Validation message", example = "Token is valid")
    private String message;
}
