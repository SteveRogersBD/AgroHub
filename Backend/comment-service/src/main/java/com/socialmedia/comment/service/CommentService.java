package com.socialmedia.comment.service;

import com.socialmedia.comment.dto.CommentListResponse;
import com.socialmedia.comment.dto.CommentResponse;
import com.socialmedia.comment.dto.CreateCommentRequest;
import com.socialmedia.comment.dto.UpdateCommentRequest;
import com.socialmedia.comment.entity.Comment;
import com.socialmedia.comment.exception.AuthorizationException;
import com.socialmedia.comment.exception.BadRequestException;
import com.socialmedia.comment.exception.ResourceNotFoundException;
import com.socialmedia.comment.mapper.CommentMapper;
import com.socialmedia.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentResponse createComment(CreateCommentRequest request, Long userId) {
        // Validate content
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BadRequestException("Content is required");
        }

        // Validate post ID
        if (request.getPostId() == null) {
            throw new BadRequestException("Post ID is required");
        }

        Comment comment = Comment.builder()
                .postId(request.getPostId())
                .userId(userId)
                .content(request.getContent())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponse(savedComment);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Authorization check - only the comment author can update
        if (!comment.getUserId().equals(userId)) {
            throw new AuthorizationException("You are not authorized to update this comment");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toResponse(updatedComment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Authorization check - only the comment author can delete
        if (!comment.getUserId().equals(userId)) {
            throw new AuthorizationException("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public CommentListResponse getCommentsByPost(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> commentPage = commentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable);

        List<CommentResponse> comments = commentPage.getContent().stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());

        return CommentListResponse.builder()
                .comments(comments)
                .currentPage(commentPage.getNumber())
                .totalPages(commentPage.getTotalPages())
                .totalElements(commentPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        return commentMapper.toResponse(comment);
    }
}
