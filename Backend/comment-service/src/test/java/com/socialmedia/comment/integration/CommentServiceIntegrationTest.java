package com.socialmedia.comment.integration;

import com.socialmedia.comment.dto.CommentListResponse;
import com.socialmedia.comment.dto.CommentResponse;
import com.socialmedia.comment.dto.CreateCommentRequest;
import com.socialmedia.comment.dto.UpdateCommentRequest;
import com.socialmedia.comment.exception.AuthorizationException;
import com.socialmedia.comment.exception.ResourceNotFoundException;
import com.socialmedia.comment.repository.CommentRepository;
import com.socialmedia.comment.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class CommentServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("comment_integration_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
    }

    @Test
    void shouldCreateCommentWithContent() {
        // Given
        Long userId = 1L;
        Long postId = 100L;
        CreateCommentRequest request = CreateCommentRequest.builder()
                .postId(postId)
                .content("This is a test comment")
                .build();

        // When
        CommentResponse response = commentService.createComment(request, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getPostId()).isEqualTo(postId);
        assertThat(response.getContent()).isEqualTo("This is a test comment");
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateCommentWhenOwner() {
        // Given
        Long userId = 1L;
        Long postId = 100L;
        CreateCommentRequest createRequest = CreateCommentRequest.builder()
                .postId(postId)
                .content("Original comment")
                .build();
        CommentResponse createdComment = commentService.createComment(createRequest, userId);

        UpdateCommentRequest updateRequest = UpdateCommentRequest.builder()
                .content("Updated comment")
                .build();

        // When
        CommentResponse updatedComment = commentService.updateComment(createdComment.getId(), updateRequest, userId);

        // Then
        assertThat(updatedComment.getContent()).isEqualTo("Updated comment");
        assertThat(updatedComment.getId()).isEqualTo(createdComment.getId());
        assertThat(updatedComment.getUpdatedAt()).isAfter(createdComment.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingOtherUsersComment() {
        // Given
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long postId = 100L;
        CreateCommentRequest createRequest = CreateCommentRequest.builder()
                .postId(postId)
                .content("User 1 comment")
                .build();
        CommentResponse createdComment = commentService.createComment(createRequest, userId1);

        UpdateCommentRequest updateRequest = UpdateCommentRequest.builder()
                .content("Trying to update")
                .build();

        // When/Then
        assertThatThrownBy(() -> commentService.updateComment(createdComment.getId(), updateRequest, userId2))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void shouldDeleteCommentWhenOwner() {
        // Given
        Long userId = 1L;
        Long postId = 100L;
        CreateCommentRequest createRequest = CreateCommentRequest.builder()
                .postId(postId)
                .content("Comment to delete")
                .build();
        CommentResponse createdComment = commentService.createComment(createRequest, userId);

        // When
        commentService.deleteComment(createdComment.getId(), userId);

        // Then
        assertThatThrownBy(() -> commentService.getCommentById(createdComment.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenDeletingOtherUsersComment() {
        // Given
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long postId = 100L;
        CreateCommentRequest createRequest = CreateCommentRequest.builder()
                .postId(postId)
                .content("User 1 comment")
                .build();
        CommentResponse createdComment = commentService.createComment(createRequest, userId1);

        // When/Then
        assertThatThrownBy(() -> commentService.deleteComment(createdComment.getId(), userId2))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void shouldGetCommentsByPostWithPagination() {
        // Given
        Long userId = 1L;
        Long postId = 100L;
        
        // Create multiple comments
        for (int i = 1; i <= 5; i++) {
            CreateCommentRequest request = CreateCommentRequest.builder()
                    .postId(postId)
                    .content("Comment " + i)
                    .build();
            commentService.createComment(request, userId);
            
            // Small delay to ensure different timestamps
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // When
        CommentListResponse response = commentService.getCommentsByPost(postId, 0, 3);

        // Then
        assertThat(response.getComments()).hasSize(3);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getTotalElements()).isEqualTo(5);
        assertThat(response.getTotalPages()).isEqualTo(2);
        
        // Verify chronological order (oldest first)
        for (int i = 0; i < response.getComments().size() - 1; i++) {
            assertThat(response.getComments().get(i).getCreatedAt())
                    .isBeforeOrEqualTo(response.getComments().get(i + 1).getCreatedAt());
        }
    }

    @Test
    void shouldReturnEmptyListWhenNoCommentsForPost() {
        // Given
        Long postId = 999L;

        // When
        CommentListResponse response = commentService.getCommentsByPost(postId, 0, 10);

        // Then
        assertThat(response.getComments()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    void shouldGetCommentById() {
        // Given
        Long userId = 1L;
        Long postId = 100L;
        CreateCommentRequest createRequest = CreateCommentRequest.builder()
                .postId(postId)
                .content("Test comment")
                .build();
        CommentResponse createdComment = commentService.createComment(createRequest, userId);

        // When
        CommentResponse retrievedComment = commentService.getCommentById(createdComment.getId());

        // Then
        assertThat(retrievedComment).isNotNull();
        assertThat(retrievedComment.getId()).isEqualTo(createdComment.getId());
        assertThat(retrievedComment.getContent()).isEqualTo("Test comment");
    }

    @Test
    void shouldThrowExceptionWhenCommentNotFound() {
        // When/Then
        assertThatThrownBy(() -> commentService.getCommentById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found");
    }
}
