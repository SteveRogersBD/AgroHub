package com.socialmedia.post.properties;

import com.socialmedia.post.dto.CreatePostRequest;
import com.socialmedia.post.dto.PostListResponse;
import com.socialmedia.post.dto.PostResponse;
import com.socialmedia.post.dto.UpdatePostRequest;
import com.socialmedia.post.entity.Post;
import com.socialmedia.post.exception.AuthorizationException;
import com.socialmedia.post.exception.ResourceNotFoundException;
import com.socialmedia.post.repository.PostRepository;
import com.socialmedia.post.service.PostService;
import net.jqwik.api.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = com.socialmedia.post.PostServiceApplication.class)
@Testcontainers
class PostServicePropertiesTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("post_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
    }

    // Feature: social-media-backend, Property 29: Post creation stores content
    // Validates: Requirements 9.1
    @Property(tries = 100)
    @Label("postCreationShouldStoreContent")
    void postCreationShouldStoreContent(
            @ForAll("validUserId") Long userId,
            @ForAll("validContent") String content,
            @ForAll("optionalMediaUrl") String mediaUrl) {
        
        // Given a valid post request
        CreatePostRequest request = CreatePostRequest.builder()
                .content(content)
                .mediaUrl(mediaUrl)
                .build();

        // When creating a post
        PostResponse response = postService.createPost(request, userId);

        // Then the post should be stored with all fields
        Assertions.assertThat(response.getUserId()).isEqualTo(userId);
        Assertions.assertThat(response.getContent()).isEqualTo(content);
        Assertions.assertThat(response.getMediaUrl()).isEqualTo(mediaUrl);
        Assertions.assertThat(response.getCreatedAt()).isNotNull();
        Assertions.assertThat(response.getUpdatedAt()).isNotNull();

        // Verify in database
        Post savedPost = postRepository.findById(response.getId()).orElseThrow();
        Assertions.assertThat(savedPost.getContent()).isEqualTo(content);
        Assertions.assertThat(savedPost.getMediaUrl()).isEqualTo(mediaUrl);
        Assertions.assertThat(savedPost.getUserId()).isEqualTo(userId);
        Assertions.assertThat(savedPost.getDeleted()).isFalse();
    }

    // Arbitraries for generating test data

    @Provide
    Arbitrary<Long> validUserId() {
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
    Arbitrary<String> optionalMediaUrl() {
        return Arbitraries.oneOf(
                Arbitraries.just((String) null),
                Arbitraries.strings()
                        .withCharRange('a', 'z')
                        .numeric()
                        .withChars('/', '.', '-', '_')
                        .ofMinLength(10)
                        .ofMaxLength(100)
                        .map(s -> "https://example.com/media/" + s + ".jpg")
        );
    }

    // Feature: social-media-backend, Property 31: Post ownership association
    // Validates: Requirements 9.4
    @Property(tries = 100)
    @Label("postShouldBeAssociatedWithCreator")
    void postShouldBeAssociatedWithCreator(
            @ForAll("validUserId") Long userId,
            @ForAll("validContent") String content) {
        
        // Given a valid post request
        CreatePostRequest request = CreatePostRequest.builder()
                .content(content)
                .build();

        // When creating a post
        PostResponse response = postService.createPost(request, userId);

        // Then the post should be associated with the user ID from JWT
        Assertions.assertThat(response.getUserId()).isEqualTo(userId);

        // Verify in database
        Post savedPost = postRepository.findById(response.getId()).orElseThrow();
        Assertions.assertThat(savedPost.getUserId()).isEqualTo(userId);
    }

    // Feature: social-media-backend, Property 34: Soft delete marks post as deleted
    // Validates: Requirements 10.3
    @Property(tries = 100)
    @Label("softDeleteShouldMarkPostAsDeleted")
    void softDeleteShouldMarkPostAsDeleted(
            @ForAll("validUserId") Long userId,
            @ForAll("validContent") String content) {
        
        // Given a created post
        CreatePostRequest request = CreatePostRequest.builder()
                .content(content)
                .build();
        PostResponse createdPost = postService.createPost(request, userId);

        // When deleting the post
        postService.deletePost(createdPost.getId(), userId);

        // Then the post should be marked as deleted in the database
        Post deletedPost = postRepository.findById(createdPost.getId()).orElseThrow();
        Assertions.assertThat(deletedPost.getDeleted()).isTrue();
    }

    // Feature: social-media-backend, Property 36: Soft-deleted posts excluded from queries
    // Validates: Requirements 10.5, 11.4
    @Property(tries = 100)
    @Label("softDeletedPostsShouldBeExcludedFromQueries")
    void softDeletedPostsShouldBeExcludedFromQueries(
            @ForAll("validUserId") Long userId,
            @ForAll("validContent") String content1,
            @ForAll("validContent") String content2) {
        
        // Given two posts, one deleted
        CreatePostRequest request1 = CreatePostRequest.builder()
                .content(content1)
                .build();
        CreatePostRequest request2 = CreatePostRequest.builder()
                .content(content2)
                .build();
        
        PostResponse post1 = postService.createPost(request1, userId);
        PostResponse post2 = postService.createPost(request2, userId);
        
        // Delete the first post
        postService.deletePost(post1.getId(), userId);

        // When querying posts by user
        Pageable pageable = PageRequest.of(0, 10);
        PostListResponse response = postService.getPostsByUserId(userId, pageable);

        // Then only the non-deleted post should be returned
        Assertions.assertThat(response.getPosts()).hasSize(1);
        Assertions.assertThat(response.getPosts().get(0).getId()).isEqualTo(post2.getId());

        // And querying by ID should not return the deleted post
        Assertions.assertThatThrownBy(() -> postService.getPostById(post1.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // Feature: social-media-backend, Property 37: User posts in reverse chronological order
    // Validates: Requirements 11.1
    @Property(tries = 50)
    @Label("userPostsShouldBeInReverseChronologicalOrder")
    void userPostsShouldBeInReverseChronologicalOrder(
            @ForAll("validUserId") Long userId,
            @ForAll("postContentList") List<String> contents) {
        
        Assume.that(contents.size() >= 3);

        // Given multiple posts created in sequence
        List<Long> postIds = new ArrayList<>();
        for (String content : contents) {
            CreatePostRequest request = CreatePostRequest.builder()
                    .content(content)
                    .build();
            PostResponse post = postService.createPost(request, userId);
            postIds.add(post.getId());
            
            // Small delay to ensure different timestamps
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // When retrieving posts by user
        Pageable pageable = PageRequest.of(0, contents.size());
        PostListResponse response = postService.getPostsByUserId(userId, pageable);

        // Then posts should be in reverse chronological order (newest first)
        List<Long> retrievedIds = response.getPosts().stream()
                .map(PostResponse::getId)
                .toList();
        
        // Reverse the created IDs to get expected order (newest first)
        List<Long> expectedIds = new ArrayList<>(postIds);
        java.util.Collections.reverse(expectedIds);
        
        Assertions.assertThat(retrievedIds).isEqualTo(expectedIds);
    }

    @Provide
    Arbitrary<List<String>> postContentList() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withChars(' ')
                .ofMinLength(5)
                .ofMaxLength(50)
                .list()
                .ofMinSize(3)
                .ofMaxSize(10);
    }
}
