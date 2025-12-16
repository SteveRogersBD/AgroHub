package com.socialmedia.comment.properties;

import com.socialmedia.comment.dto.CommentListResponse;
import com.socialmedia.comment.dto.CommentResponse;
import com.socialmedia.comment.dto.CreateCommentRequest;
import com.socialmedia.comment.dto.UpdateCommentRequest;
import com.socialmedia.comment.entity.Comment;
import com.socialmedia.comment.exception.AuthorizationException;
import com.socialmedia.comment.repository.CommentRepository;
import com.socialmedia.comment.service.CommentService;
import net.jqwik.api.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = com.socialmedia.comment.CommentServiceApplication.class)
@Testcontainers
@ExtendWith(SpringExtension.class)
class CommentServicePropertiesTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("comment_test_db")
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

    // Feature: social-media-backend, Property 39: Comment creation stores content
    // Validates: Requirements 12.1
    @Property(tries = 100)
    @Label("commentCreationShouldStoreContent")
    void commentCreationShouldStoreContent(
            @ForAll("validUserId") Long userId,
            @ForAll("validPostId") Long postId,
            @ForAll("validContent") String content) {
        
        // Given a valid comment request
        CreateCommentRequest request = CreateCommentRequest.builder()
                .postId(postId)
                .content(content)
                .build();

        // When creating a comment
        CommentResponse response = commentService.createComment(request, userId);

        // Then the comment should be stored with all fields
        Assertions.assertThat(response.getUserId()).isEqualTo(userId);
        Assertions.assertThat(response.getPostId()).isEqualTo(postId);
        Assertions.assertThat(response.getContent()).isEqualTo(content);
        Assertions.assertThat(response.getCreatedAt()).isNotNull();
        Assertions.assertThat(response.getUpdatedAt()).isNotNull();

        // Verify in database
        Comment savedComment = commentRepository.findById(response.getId()).orElseThrow();
        Assertions.assertThat(savedComment.getContent()).isEqualTo(content);
        Assertions.assertThat(savedComment.getPostId()).isEqualTo(postId);
        Assertions.assertThat(savedComment.getUserId()).isEqualTo(userId);
    }

    // Feature: social-media-backend, Property 40: Comment ownership association
    // Validates: Requirements 12.4
    @Property(tries = 100)
    @Label("commentShouldBeAssociatedWithCreator")
    void commentShouldBeAssociatedWithCreator(
            @ForAll("validUserId") Long userId,
            @ForAll("validPostId") Long postId,
            @ForAll("validContent") String content) {
        
        // Given a valid comment request
        CreateCommentRequest request = CreateCommentRequest.builder()
                .postId(postId)
                .content(content)
                .build();

        // When creating a comment
        CommentResponse response = commentService.createComment(request, userId);

        // Then the comment should be associated with the user ID from JWT
        Assertions.assertThat(response.getUserId()).isEqualTo(userId);

        // Verify in database
        Comment savedComment = commentRepository.findById(response.getId()).orElseThrow();
        Assertions.assertThat(savedComment.getUserId()).isEqualTo(userId);
    }

    // Feature: social-media-backend, Property 42: Cross-user comment update rejection
    // Validates: Requirements 13.2
    @Property(tries = 100)
    @Label("crossUserCommentUpdateShouldBeRejected")
    void crossUserCommentUpdateShouldBeRejected(
            @ForAll("validUserId") Long userId1,
            @ForAll("validUserId") Long userId2,
            @ForAll("validPostId") Long postId,
            @ForAll("validContent") String content,
            @ForAll("validContent") String newContent) {
        
        Assume.that(!userId1.equals(userId2));

        // Given a comment created by user1
        CreateCommentRequest createRequest = CreateCommentRequest.builder()
                .postId(postId)
                .content(content)
                .build();
        CommentResponse createdComment = commentService.createComment(createRequest, userId1);

        // When user2 attempts to update the comment
        UpdateCommentRequest updateRequest = UpdateCommentRequest.builder()
                .content(newContent)
                .build();

        // Then the update should be rejected with an authorization error
        Assertions.assertThatThrownBy(() -> 
                commentService.updateComment(createdComment.getId(), updateRequest, userId2))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("not authorized");
    }

    // Feature: social-media-backend, Property 45: Comments in chronological order
    // Validates: Requirements 14.1
    @Property(tries = 100)
    @Label("commentsShouldBeInChronologicalOrder")
    void commentsShouldBeInChronologicalOrder(
            @ForAll("validUserId") Long userId,
            @ForAll("validPostId") Long postId,
            @ForAll("commentContentList") List<String> contents) {
        
        Assume.that(contents.size() >= 2);

        // Given multiple comments on the same post
        List<Long> commentIds = new ArrayList<>();
        for (String content : contents) {
            CreateCommentRequest request = CreateCommentRequest.builder()
                    .postId(postId)
                    .content(content)
                    .build();
            CommentResponse response = commentService.createComment(request, userId);
            commentIds.add(response.getId());
            
            // Small delay to ensure different timestamps
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // When retrieving comments for the post
        CommentListResponse response = commentService.getCommentsByPost(postId, 0, 100);

        // Then comments should be in chronological order (oldest first)
        List<CommentResponse> retrievedComments = response.getComments();
        Assertions.assertThat(retrievedComments).hasSizeGreaterThanOrEqualTo(contents.size());
        
        for (int i = 0; i < retrievedComments.size() - 1; i++) {
            Assertions.assertThat(retrievedComments.get(i).getCreatedAt())
                    .isBeforeOrEqualTo(retrievedComments.get(i + 1).getCreatedAt());
        }
    }

    // Arbitraries for generating test data

    @Provide
    Arbitrary<Long> validUserId() {
        return Arbitraries.longs().between(1L, 1000000L);
    }

    @Provide
    Arbitrary<Long> validPostId() {
        return Arbitraries.longs().between(1L, 1000000L);
    }

    @Provide
    Arbitrary<String> validContent() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withChars(' ', '.', ',', '!', '?', '\n')
                .ofMinLength(1)
                .ofMaxLength(500);
    }

    @Provide
    Arbitrary<List<String>> commentContentList() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withChars(' ', '.', ',')
                .ofMinLength(1)
                .ofMaxLength(100)
                .list()
                .ofMinSize(2)
                .ofMaxSize(5);
    }
}
