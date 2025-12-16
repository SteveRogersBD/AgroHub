package com.socialmedia.post.integration;

import com.socialmedia.post.dto.CreatePostRequest;
import com.socialmedia.post.dto.PostListResponse;
import com.socialmedia.post.dto.PostResponse;
import com.socialmedia.post.dto.UpdatePostRequest;
import com.socialmedia.post.entity.Post;
import com.socialmedia.post.exception.AuthorizationException;
import com.socialmedia.post.exception.ResourceNotFoundException;
import com.socialmedia.post.repository.PostRepository;
import com.socialmedia.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class PostServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("post_integration_test_db")
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

    @Test
    void shouldCreatePostWithContent() {
        // Given
        Long userId = 1L;
        CreatePostRequest request = CreatePostRequest.builder()
                .content("This is a test post")
                .build();

        // When
        PostResponse response = postService.createPost(request, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getContent()).isEqualTo("This is a test post");
        assertThat(response.getMediaUrl()).isNull();
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCreatePostWithContentAndMedia() {
        // Given
        Long userId = 1L;
        CreatePostRequest request = CreatePostRequest.builder()
                .content("Post with media")
                .mediaUrl("https://example.com/image.jpg")
                .build();

        // When
        PostResponse response = postService.createPost(request, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Post with media");
        assertThat(response.getMediaUrl()).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    void shouldUpdatePostWhenOwner() {
        // Given
        Long userId = 1L;
        CreatePostRequest createRequest = CreatePostRequest.builder()
                .content("Original content")
                .build();
        PostResponse createdPost = postService.createPost(createRequest, userId);

        UpdatePostRequest updateRequest = UpdatePostRequest.builder()
                .content("Updated content")
                .mediaUrl("https://example.com/new-image.jpg")
                .build();

        // When
        PostResponse updatedPost = postService.updatePost(createdPost.getId(), updateRequest, userId);

        // Then
        assertThat(updatedPost.getId()).isEqualTo(createdPost.getId());
        assertThat(updatedPost.getContent()).isEqualTo("Updated content");
        assertThat(updatedPost.getMediaUrl()).isEqualTo("https://example.com/new-image.jpg");
        assertThat(updatedPost.getUpdatedAt()).isAfter(createdPost.getUpdatedAt());
    }

    @Test
    void shouldThrowAuthorizationExceptionWhenUpdatingOthersPost() {
        // Given
        Long ownerUserId = 1L;
        Long otherUserId = 2L;
        CreatePostRequest createRequest = CreatePostRequest.builder()
                .content("Owner's post")
                .build();
        PostResponse createdPost = postService.createPost(createRequest, ownerUserId);

        UpdatePostRequest updateRequest = UpdatePostRequest.builder()
                .content("Trying to update")
                .build();

        // When/Then
        assertThatThrownBy(() -> postService.updatePost(createdPost.getId(), updateRequest, otherUserId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void shouldSoftDeletePost() {
        // Given
        Long userId = 1L;
        CreatePostRequest createRequest = CreatePostRequest.builder()
                .content("Post to delete")
                .build();
        PostResponse createdPost = postService.createPost(createRequest, userId);

        // When
        postService.deletePost(createdPost.getId(), userId);

        // Then
        Post deletedPost = postRepository.findById(createdPost.getId()).orElseThrow();
        assertThat(deletedPost.getDeleted()).isTrue();

        // And should not be retrievable via service
        assertThatThrownBy(() -> postService.getPostById(createdPost.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowAuthorizationExceptionWhenDeletingOthersPost() {
        // Given
        Long ownerUserId = 1L;
        Long otherUserId = 2L;
        CreatePostRequest createRequest = CreatePostRequest.builder()
                .content("Owner's post")
                .build();
        PostResponse createdPost = postService.createPost(createRequest, ownerUserId);

        // When/Then
        assertThatThrownBy(() -> postService.deletePost(createdPost.getId(), otherUserId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void shouldRetrievePostById() {
        // Given
        Long userId = 1L;
        CreatePostRequest createRequest = CreatePostRequest.builder()
                .content("Test post")
                .build();
        PostResponse createdPost = postService.createPost(createRequest, userId);

        // When
        PostResponse retrievedPost = postService.getPostById(createdPost.getId());

        // Then
        assertThat(retrievedPost.getId()).isEqualTo(createdPost.getId());
        assertThat(retrievedPost.getContent()).isEqualTo(createdPost.getContent());
    }

    @Test
    void shouldGetPostsByUserIdWithPagination() {
        // Given
        Long userId = 1L;
        for (int i = 0; i < 5; i++) {
            CreatePostRequest request = CreatePostRequest.builder()
                    .content("Post " + i)
                    .build();
            postService.createPost(request, userId);
        }

        // When
        Pageable pageable = PageRequest.of(0, 3);
        PostListResponse response = postService.getPostsByUserId(userId, pageable);

        // Then
        assertThat(response.getPosts()).hasSize(3);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getTotalElements()).isEqualTo(5);
        assertThat(response.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldExcludeSoftDeletedPostsFromUserPosts() {
        // Given
        Long userId = 1L;
        CreatePostRequest request1 = CreatePostRequest.builder()
                .content("Post 1")
                .build();
        CreatePostRequest request2 = CreatePostRequest.builder()
                .content("Post 2")
                .build();
        CreatePostRequest request3 = CreatePostRequest.builder()
                .content("Post 3")
                .build();

        PostResponse post1 = postService.createPost(request1, userId);
        PostResponse post2 = postService.createPost(request2, userId);
        PostResponse post3 = postService.createPost(request3, userId);

        // Delete post2
        postService.deletePost(post2.getId(), userId);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        PostListResponse response = postService.getPostsByUserId(userId, pageable);

        // Then
        assertThat(response.getPosts()).hasSize(2);
        assertThat(response.getPosts())
                .extracting(PostResponse::getId)
                .containsExactlyInAnyOrder(post1.getId(), post3.getId());
    }

    @Test
    void shouldReturnPostsInReverseChronologicalOrder() throws InterruptedException {
        // Given
        Long userId = 1L;
        PostResponse post1 = postService.createPost(
                CreatePostRequest.builder().content("First post").build(), userId);
        Thread.sleep(10);
        PostResponse post2 = postService.createPost(
                CreatePostRequest.builder().content("Second post").build(), userId);
        Thread.sleep(10);
        PostResponse post3 = postService.createPost(
                CreatePostRequest.builder().content("Third post").build(), userId);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        PostListResponse response = postService.getPostsByUserId(userId, pageable);

        // Then
        assertThat(response.getPosts()).hasSize(3);
        assertThat(response.getPosts().get(0).getId()).isEqualTo(post3.getId());
        assertThat(response.getPosts().get(1).getId()).isEqualTo(post2.getId());
        assertThat(response.getPosts().get(2).getId()).isEqualTo(post1.getId());
    }
}
