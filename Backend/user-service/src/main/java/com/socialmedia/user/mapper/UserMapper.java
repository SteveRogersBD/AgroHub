package com.socialmedia.user.mapper;

import com.socialmedia.user.dto.UserProfileRequest;
import com.socialmedia.user.dto.UserProfileResponse;
import com.socialmedia.user.entity.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfile toEntity(UserProfileRequest request, Long userId) {
        return UserProfile.builder()
                .userId(userId)
                .name(request.getName())
                .bio(request.getBio())
                .avatarUrl(request.getAvatarUrl())
                .location(request.getLocation())
                .website(request.getWebsite())
                .build();
    }

    public UserProfileResponse toResponse(UserProfile entity) {
        return UserProfileResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .bio(entity.getBio())
                .avatarUrl(entity.getAvatarUrl())
                .location(entity.getLocation())
                .website(entity.getWebsite())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntity(UserProfile entity, UserProfileRequest request) {
        entity.setName(request.getName());
        entity.setBio(request.getBio());
        entity.setAvatarUrl(request.getAvatarUrl());
        entity.setLocation(request.getLocation());
        entity.setWebsite(request.getWebsite());
    }
}
