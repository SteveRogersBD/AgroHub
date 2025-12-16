package com.socialmedia.like.integration;

import com.socialmedia.like.dto.*;
import com.socialmedia.like.entity.Like;
import com.socialmedia.like.repository.LikeRepository;
import com.socialmedia.like.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class LikeServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("like_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeRepository likeRepository;

    @BeforeEach
    void setUp() {
        likeRepository.deleteAll();
    }

    @Test
    void testLikePost_CreatesLikeRecord() {
        // Given
        Long postId = 1L;
        Long userId = 100L;

        // When
        LikeResponse response = likeService.likePost(postId, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPostId()).isEqualTo(postId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getCreatedAt()).isNotNull();

        // Verify in database
        Optional<Like> savedLike = likeRepository.findByPostIdAndUserId(postId, userId);
        assertThat(savedLike).isPresent();
        assertThat(savedLike.get().getPostId()).isEqualTo(postId);
        assertThat(savedLike.get().getUserId()).isEqualTo(userId);
    }

    @Test
    void testLikePost_Idempotent() {
        // Given
        Long postId = 1L;
        Long userId = 100L;

        // When - like the same post twice
        LikeResponse response1 = likeService.likePost(postId, userId);
        LikeResponse response2 = likeService.likePost(postId, userId);

        // Then - should have exactly one like record
        assertThat(response1.getPostId()).isEqualTo(postId);
        assertThat(response2.getPostId()).isEqualTo(postId);

        long count = likeRepository.countByPostId(postId);
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void testUnlikePost_RemovesLikeRecord() {
        // Given - a user has liked a post
        Long postId = 1L;
        Long userId = 100L;
        likeService.likePost(postId, userId);

        // Verify like exists
        Optional<Like> likeBeforeUnlike = likeRepository.findByPostIdAndUserId(postId, userId);
        assertThat(likeBeforeUnlike).isPresent();

        // When - user unlikes the post
        likeService.unlikePost(postId, userId);

        // Then - like record should be removed
        Optional<Like> likeAfterUnlike = likeRepository.findByPostIdAndUserId(postId, userId);
        assertThat(likeAfterUnlike).isEmpty();
    }

    @Test
    void testUnlikePost_Idempotent() {
        // Given
        Long postId = 1L;
        Long userId = 100L;

        // When - unlike a post that was never liked
        likeService.unlikePost(postId, userId);

        // Then - should complete without error
        Optional<Like> like = likeRepository.findByPostIdAndUserId(postId, userId);
        assertThat(like).isEmpty();
    }

    @Test
    void testGetLikeCount_ReturnsAccurateCount() {
        // Given - multiple users like a post
        Long postId = 1L;
        likeService.likePost(postId, 100L);
        likeService.likePost(postId, 101L);
        likeService.likePost(postId, 102L);

        // When
        LikeCountResponse response = likeService.getLikeCount(postId);

        // Then
        assertThat(response.getPostId()).isEqualTo(postId);
        assertThat(response.getCount()).isEqualTo(3L);
    }

    @Test
    void testCheckIfUserLikedPost_ReturnsTrue_WhenLiked() {
        // Given
        Long postId = 1L;
        Long userId = 100L;
        likeService.likePost(postId, userId);

        // When
        LikeCheckResponse response = likeService.checkIfUserLikedPost(postId, userId);

        // Then
        assertThat(response.getPostId()).isEqualTo(postId);
        assertThat(response.getLiked()).isTrue();
    }

    @Test
    void testCheckIfUserLikedPost_ReturnsFalse_WhenNotLiked() {
        // Given
        Long postId = 1L;
        Long userId = 100L;

        // When
        LikeCheckResponse response = likeService.checkIfUserLikedPost(postId, userId);

        // Then
        assertThat(response.getPostId()).isEqualTo(postId);
        assertThat(response.getLiked()).isFalse();
    }

    @Test
    void testGetBatchLikeCounts_ReturnsCountsForAllPosts() {
        // Given - multiple posts with different like counts
        Long post1 = 1L;
        Long post2 = 2L;
        Long post3 = 3L;

        likeService.likePost(post1, 100L);
        likeService.likePost(post1, 101L);

        likeService.likePost(post2, 100L);

        // post3 has no likes

        // When
        List<Long> postIds = Arrays.asList(post1, post2, post3);
        BatchLikeCountResponse response = likeService.getBatchLikeCounts(postIds);

        // Then
        assertThat(response.getLikeCounts()).hasSize(3);
        assertThat(response.getLikeCounts().get(post1)).isEqualTo(2L);
        assertThat(response.getLikeCounts().get(post2)).isEqualTo(1L);
        assertThat(response.getLikeCounts().get(post3)).isEqualTo(0L);
    }
}
