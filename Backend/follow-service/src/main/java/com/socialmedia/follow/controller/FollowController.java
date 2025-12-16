package com.socialmedia.follow.controller;

import com.socialmedia.follow.dto.*;
import com.socialmedia.follow.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
@Tag(name = "Follow", description = "Follow relationship management endpoints")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{userId}")
    @Operation(summary = "Follow a user", description = "Create a follow relationship with another user")
    @ApiResponse(responseCode = "201", description = "Successfully followed user")
    @ApiResponse(responseCode = "400", description = "Invalid request (e.g., trying to follow self)")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<FollowResponse> followUser(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long currentUserId) {
        FollowResponse response = followService.follow(currentUserId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Unfollow a user", description = "Remove a follow relationship with another user")
    @ApiResponse(responseCode = "204", description = "Successfully unfollowed user")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long currentUserId) {
        followService.unfollow(currentUserId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/followers")
    @Operation(summary = "Get followers", description = "Get paginated list of users who follow the specified user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved followers")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<FollowerListResponse> getFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        FollowerListResponse response = followService.getFollowers(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/following")
    @Operation(summary = "Get following", description = "Get paginated list of users that the specified user follows")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved following")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<FollowingListResponse> getFollowing(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        FollowingListResponse response = followService.getFollowing(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/stats")
    @Operation(summary = "Get follow statistics", description = "Get follower and following counts for a user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<FollowStatsResponse> getFollowStats(@PathVariable Long userId) {
        FollowStatsResponse response = followService.getFollowStats(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check/{userId}")
    @Operation(summary = "Check if following", description = "Check if current user follows the specified user")
    @ApiResponse(responseCode = "200", description = "Successfully checked follow status")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<FollowCheckResponse> checkIfFollowing(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long currentUserId) {
        FollowCheckResponse response = followService.checkIfFollowing(currentUserId, userId);
        return ResponseEntity.ok(response);
    }
}
