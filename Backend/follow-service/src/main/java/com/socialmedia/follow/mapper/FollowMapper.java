package com.socialmedia.follow.mapper;

import com.socialmedia.follow.dto.FollowResponse;
import com.socialmedia.follow.entity.Follow;
import org.springframework.stereotype.Component;

@Component
public class FollowMapper {

    public FollowResponse toResponse(Follow follow) {
        if (follow == null) {
            return null;
        }

        return FollowResponse.builder()
                .id(follow.getId())
                .followerId(follow.getFollowerId())
                .followingId(follow.getFollowingId())
                .createdAt(follow.getCreatedAt())
                .build();
    }

    public Follow toEntity(Long followerId, Long followingId) {
        return Follow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
    }
}
