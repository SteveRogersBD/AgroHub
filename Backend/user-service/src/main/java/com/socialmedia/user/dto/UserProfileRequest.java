package com.socialmedia.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User profile creation/update request")
public class UserProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "User's display name", example = "John Doe", required = true, maxLength = 100)
    private String name;

    @Size(max = 5000, message = "Bio must not exceed 5000 characters")
    @Schema(description = "User biography", example = "Software developer and tech enthusiast", maxLength = 5000)
    private String bio;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    @Schema(description = "URL to user's avatar image", example = "https://example.com/avatar.jpg", maxLength = 500)
    private String avatarUrl;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    @Schema(description = "User's location", example = "San Francisco, CA", maxLength = 100)
    private String location;

    @Size(max = 255, message = "Website must not exceed 255 characters")
    @Schema(description = "User's website URL", example = "https://johndoe.com", maxLength = 255)
    private String website;
}
