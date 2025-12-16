package com.socialmedia.post.controller;

import com.socialmedia.post.dto.CreatePostRequest;
import com.socialmedia.post.dto.PostListResponse;
import com.socialmedia.post.dto.PostResponse;
import com.socialmedia.post.dto.UpdatePostRequest;
import com.socialmedia.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Post Management", description = "APIs for managing posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "Create a new post", description = "Create a new post with content and optional media")
    @ApiResponse(responseCode = "201", description = "Post created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        PostResponse response = postService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a post", description = "Update an existing post")
    @ApiResponse(responseCode = "200", description = "Post updated successfully")
    @ApiResponse(responseCode = "403", description = "Not authorized to update this post")
    @ApiResponse(responseCode = "404", description = "Post not found")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        PostResponse response = postService.updatePost(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a post", description = "Soft delete a post")
    @ApiResponse(responseCode = "204", description = "Post deleted successfully")
    @ApiResponse(responseCode = "403", description = "Not authorized to delete this post")
    @ApiResponse(responseCode = "404", description = "Post not found")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get post by ID", description = "Retrieve a post by its ID")
    @ApiResponse(responseCode = "200", description = "Post retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Post not found")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse response = postService.getPostById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get posts by user", description = "Retrieve all posts by a specific user with pagination")
    @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
    public ResponseEntity<PostListResponse> getPostsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PostListResponse response = postService.getPostsByUserId(userId, pageable);
        return ResponseEntity.ok(response);
    }
}
