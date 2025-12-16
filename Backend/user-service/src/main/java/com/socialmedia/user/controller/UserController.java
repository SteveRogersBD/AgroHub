package com.socialmedia.user.controller;

import com.socialmedia.user.dto.UserProfileRequest;
import com.socialmedia.user.dto.UserProfileResponse;
import com.socialmedia.user.dto.UserSearchResponse;
import com.socialmedia.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management endpoints")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create user profile", description = "Create a new user profile for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Profile created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileResponse> createProfile(
            @Valid @RequestBody UserProfileRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        UserProfileResponse response = userService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by ID", description = "Retrieve a user profile by profile ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<UserProfileResponse> getProfileById(@PathVariable Long id) {
        UserProfileResponse response = userService.getProfileById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user profile by user ID", description = "Retrieve a user profile by user ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<UserProfileResponse> getProfileByUserId(@PathVariable Long userId) {
        UserProfileResponse response = userService.getProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieve the profile of the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(@RequestHeader("X-User-Id") Long userId) {
        UserProfileResponse response = userService.getCurrentUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Update an existing user profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - cannot update another user's profile"),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileRequest request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        UserProfileResponse response = userService.updateProfile(id, currentUserId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by name", description = "Search for users by name with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<UserSearchResponse> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UserSearchResponse response = userService.searchByName(name, page, size);
        return ResponseEntity.ok(response);
    }
}
