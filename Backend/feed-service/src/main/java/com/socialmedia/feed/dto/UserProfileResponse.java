package com.socialmedia.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private Long userId;
    private String name;
    private String bio;
    private String avatarUrl;
    private String location;
    private String website;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
