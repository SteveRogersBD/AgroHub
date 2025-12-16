package com.socialmedia.post.mapper;

import com.socialmedia.post.dto.CreatePostRequest;
import com.socialmedia.post.dto.PostResponse;
import com.socialmedia.post.dto.UpdatePostRequest;
import com.socialmedia.post.entity.Post;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public Post toEntity(CreatePostRequest request, Long userId) {
        return Post.builder()
                .userId(userId)
                .content(request.getContent())
                .mediaUrl(request.getMediaUrl())
                .deleted(false)
                .build();
    }

    public void updateEntity(Post post, UpdatePostRequest request) {
        post.setContent(request.getContent());
        post.setMediaUrl(request.getMediaUrl());
    }

    public PostResponse toResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .content(post.getContent())
                .mediaUrl(post.getMediaUrl())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
