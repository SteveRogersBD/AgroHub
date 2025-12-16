package com.socialmedia.post.service;

import com.socialmedia.post.dto.CreatePostRequest;
import com.socialmedia.post.dto.PostListResponse;
import com.socialmedia.post.dto.PostResponse;
import com.socialmedia.post.dto.UpdatePostRequest;
import com.socialmedia.post.entity.Post;
import com.socialmedia.post.exception.AuthorizationException;
import com.socialmedia.post.exception.ResourceNotFoundException;
import com.socialmedia.post.mapper.PostMapper;
import com.socialmedia.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Transactional
    public PostResponse createPost(CreatePostRequest request, Long userId) {
        // Validate content is not empty (additional check beyond @NotBlank)
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }

        Post post = postMapper.toEntity(request, userId);
        Post savedPost = postRepository.save(post);
        return postMapper.toResponse(savedPost);
    }

    @Transactional
    public PostResponse updatePost(Long postId, UpdatePostRequest request, Long userId) {
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Authorization check: only the post owner can update
        if (!post.getUserId().equals(userId)) {
            throw new AuthorizationException("You are not authorized to update this post");
        }

        postMapper.updateEntity(post, request);
        Post updatedPost = postRepository.save(post);
        return postMapper.toResponse(updatedPost);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Authorization check: only the post owner can delete
        if (!post.getUserId().equals(userId)) {
            throw new AuthorizationException("You are not authorized to delete this post");
        }

        // Soft delete: mark as deleted
        post.setDeleted(true);
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        return postMapper.toResponse(post);
    }

    @Transactional(readOnly = true)
    public PostListResponse getPostsByUserId(Long userId, Pageable pageable) {
        Page<Post> postPage = postRepository.findByUserIdAndNotDeleted(userId, pageable);
        
        List<PostResponse> posts = postPage.getContent().stream()
                .map(postMapper::toResponse)
                .collect(Collectors.toList());

        return PostListResponse.builder()
                .posts(posts)
                .currentPage(postPage.getNumber())
                .totalPages(postPage.getTotalPages())
                .totalElements(postPage.getTotalElements())
                .build();
    }
}
