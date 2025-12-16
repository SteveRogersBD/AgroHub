package com.socialmedia.comment.controller;

import com.socialmedia.comment.dto.CommentListResponse;
import com.socialmedia.comment.dto.CommentResponse;
import com.socialmedia.comment.dto.CreateCommentRequest;
import com.socialmedia.comment.dto.UpdateCommentRequest;
import com.socialmedia.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "Comment management endpoints")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Create a new comment", description = "Create a new comment on a post")
    @ApiResponse(responseCode = "201", description = "Comment created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        CommentResponse response = commentService.createComment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a comment", description = "Update an existing comment")
    @ApiResponse(responseCode = "200", description = "Comment updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - not the comment author")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommentRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        CommentResponse response = commentService.updateComment(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a comment", description = "Delete an existing comment")
    @ApiResponse(responseCode = "204", description = "Comment deleted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - not the comment author")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        commentService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Get comments for a post", description = "Get paginated list of comments for a specific post")
    @ApiResponse(responseCode = "200", description = "Comments retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<CommentListResponse> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        CommentListResponse response = commentService.getCommentsByPost(postId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get comment by ID", description = "Get a specific comment by its ID")
    @ApiResponse(responseCode = "200", description = "Comment retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long id) {
        CommentResponse response = commentService.getCommentById(id);
        return ResponseEntity.ok(response);
    }
}
