package com.socialmedia.feed.controller;

import com.socialmedia.feed.dto.FeedResponse;
import com.socialmedia.feed.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Feed", description = "Feed generation and retrieval endpoints")
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    @Operation(summary = "Get personalized feed", description = "Retrieve a paginated feed of posts from users the current user follows")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feed retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<FeedResponse> getFeed(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-User-Id") Long userId,
            HttpServletRequest request) {
        
        log.info("Fetching feed for user {} - page: {}, size: {}", userId, page, size);

        // Extract token from request
        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : "";

        FeedResponse feed = feedService.generateFeed(userId, page, size, token);
        
        return ResponseEntity.ok(feed);
    }
}
