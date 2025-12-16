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
public class EnrichedPostResponse {

    private Long id;
    private Long userId;
    private String username;
    private String userAvatarUrl;
    private String content;
    private String mediaUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Enriched metadata
    private Long likeCount;
    private Long commentCount;
    private Boolean likedByCurrentUser;
}
