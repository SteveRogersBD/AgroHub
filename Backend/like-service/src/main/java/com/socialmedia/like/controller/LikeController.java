package com.socialmedia.like.controller;

import com.socialmedia.like.dto.*;
import com.socialmedia.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Like", description = "Like management APIs")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{postId}")
    @Operation(summary = "Like a post", description = "Like a post (idempotent operation)")
    public ResponseEntity<LikeResponse> likePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("User {} liking post {}", userId, postId);
        
        LikeResponse response = likeService.likePost(postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Unlike a post", description = "Unlike a post (idempotent operation)")
    public ResponseEntity<Void> unlikePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("User {} unliking post {}", userId, postId);
        
        likeService.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/count")
    @Operation(summary = "Get like count for a post", description = "Get the total number of likes for a post")
    public ResponseEntity<LikeCountResponse> getLikeCount(@PathVariable Long postId) {
        log.info("Getting like count for post {}", postId);
        
        LikeCountResponse response = likeService.getLikeCount(postId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/check")
    @Operation(summary = "Check if user liked a post", description = "Check if the current user has liked a post")
    public ResponseEntity<LikeCheckResponse> checkIfUserLikedPost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Checking if user {} liked post {}", userId, postId);
        
        LikeCheckResponse response = likeService.checkIfUserLikedPost(postId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch/counts")
    @Operation(summary = "Get like counts for multiple posts", description = "Get like counts for a batch of posts")
    public ResponseEntity<BatchLikeCountResponse> getBatchLikeCounts(
            @Valid @RequestBody BatchLikeCountRequest request) {
        
        log.info("Getting like counts for {} posts", request.getPostIds().size());
        
        BatchLikeCountResponse response = likeService.getBatchLikeCounts(request.getPostIds());
        return ResponseEntity.ok(response);
    }
}
