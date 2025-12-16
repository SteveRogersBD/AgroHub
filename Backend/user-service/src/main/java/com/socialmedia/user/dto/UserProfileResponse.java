package com.socialmedia.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User profile response")
public class UserProfileResponse {

    @Schema(description = "Profile ID", example = "1")
    private Long id;
    
    @Schema(description = "User ID", example = "1")
    private Long userId;
    
    @Schema(description = "User's display name", example = "John Doe")
    private String name;
    
    @Schema(description = "User biography", example = "Software developer and tech enthusiast")
    private String bio;
    
    @Schema(description = "URL to user's avatar image", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
    
    @Schema(description = "User's location", example = "San Francisco, CA")
    private String location;
    
    @Schema(description = "User's website URL", example = "https://johndoe.com")
    private String website;
    
    @Schema(description = "Profile creation timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Profile last update timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
}
