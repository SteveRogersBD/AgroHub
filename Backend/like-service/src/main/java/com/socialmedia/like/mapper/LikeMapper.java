package com.socialmedia.like.mapper;

import com.socialmedia.like.dto.LikeResponse;
import com.socialmedia.like.entity.Like;
import org.springframework.stereotype.Component;

@Component
public class LikeMapper {

    public LikeResponse toResponse(Like like) {
        if (like == null) {
            return null;
        }

        return LikeResponse.builder()
                .id(like.getId())
                .postId(like.getPostId())
                .userId(like.getUserId())
                .createdAt(like.getCreatedAt())
                .build();
    }

    public Like toEntity(Long postId, Long userId) {
        return Like.builder()
                .postId(postId)
                .userId(userId)
                .build();
    }
}
