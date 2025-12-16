package com.socialmedia.like.properties;

import com.socialmedia.like.dto.LikeCheckResponse;
import com.socialmedia.like.dto.LikeCountResponse;
import com.socialmedia.like.dto.LikeResponse;
import com.socialmedia.like.entity.Like;
import com.socialmedia.like.repository.LikeRepository;
import com.socialmedia.like.service.LikeService;
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

import java.util.Optional;

@SpringBootTest(classes = com.socialmedia.like.LikeServiceApplication.class)
@Testcontainers
@ExtendWith(SpringExtension.class)
class LikeServicePropertiesTest {

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

    // Feature: social-media-backend, Property 48: Like creates record
    // Validates: Requirements 15.1
    @Property(tries = 100)
    @Label("likeShouldCreateRecord")
    void likeShouldCreateRecord(
            @ForAll("validPostId") Long postId,
            @ForAll("validUserId") Long userId) {
        
        // When a user likes a post
        LikeResponse response = likeService.likePost(postId, userId);

        // Then a like record should be created
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getPostId()).isEqualTo(postId);
        Assertions.assertThat(response.getUserId()).isEqualTo(userId);
        Assertions.assertThat(response.getCreatedAt()).isNotNull();

        // Verify in database
        Optional<Like> savedLike = likeRepository.findByPostIdAndUserId(postId, userId);
        Assertions.assertThat(savedLike).isPresent();
        Assertions.assertThat(savedLike.get().getPostId()).isEqualTo(postId);
        Assertions.assertThat(savedLike.get().getUserId()).isEqualTo(userId);
    }

    // Feature: social-media-backend, Property 49: Like idempotence
    // Validates: Requirements 15.2
    @Property(tries = 100)
    @Label("likeShouldBeIdempotent")
    void likeShouldBeIdempotent(
            @ForAll("validPostId") Long postId,
            @ForAll("validUserId") Long userId) {
        
        // When a user likes the same post twice
        LikeResponse response1 = likeService.likePost(postId, userId);
        LikeResponse response2 = likeService.likePost(postId, userId);

        // Then only one like record should exist
        Assertions.assertThat(response1.getPostId()).isEqualTo(postId);
        Assertions.assertThat(response2.getPostId()).isEqualTo(postId);

        // Verify in database - should have exactly one record
        long count = likeRepository.countByPostId(postId);
        Assertions.assertThat(count).isEqualTo(1L);

        Optional<Like> savedLike = likeRepository.findByPostIdAndUserId(postId, userId);
        Assertions.assertThat(savedLike).isPresent();
    }

    // Feature: social-media-backend, Property 50: Unlike removes record
    // Validates: Requirements 15.3
    @Property(tries = 100)
    @Label("unlikeShouldRemoveRecord")
    void unlikeShouldRemoveRecord(
            @ForAll("validPostId") Long postId,
            @ForAll("validUserId") Long userId) {
        
        // Given a user has liked a post
        likeService.likePost(postId, userId);
        
        // Verify like exists
        Optional<Like> likeBeforeUnlike = likeRepository.findByPostIdAndUserId(postId, userId);
        Assertions.assertThat(likeBeforeUnlike).isPresent();

        // When the user unlikes the post
        likeService.unlikePost(postId, userId);

        // Then the like record should be removed from the database
        Optional<Like> likeAfterUnlike = likeRepository.findByPostIdAndUserId(postId, userId);
        Assertions.assertThat(likeAfterUnlike).isEmpty();
    }

    // Feature: social-media-backend, Property 52: Like count accuracy
    // Validates: Requirements 16.1
    @Property(tries = 100)
    @Label("likeCountShouldBeAccurate")
    void likeCountShouldBeAccurate(
            @ForAll("validPostId") Long postId,
            @ForAll("userIdList") java.util.List<Long> userIds) {
        
        Assume.that(userIds.size() >= 1);
        Assume.that(userIds.stream().distinct().count() == userIds.size()); // All unique

        // Given multiple users like a post
        for (Long userId : userIds) {
            likeService.likePost(postId, userId);
        }

        // When getting the like count
        LikeCountResponse response = likeService.getLikeCount(postId);

        // Then the count should equal the number of users who liked it
        Assertions.assertThat(response.getPostId()).isEqualTo(postId);
        Assertions.assertThat(response.getCount()).isEqualTo((long) userIds.size());

        // Verify in database
        long dbCount = likeRepository.countByPostId(postId);
        Assertions.assertThat(dbCount).isEqualTo((long) userIds.size());
    }

    // Arbitraries for generating test data

    @Provide
    Arbitrary<Long> validPostId() {
        return Arbitraries.longs().between(1L, 1000000L);
    }

    @Provide
    Arbitrary<Long> validUserId() {
        return Arbitraries.longs().between(1L, 1000000L);
    }

    @Provide
    Arbitrary<java.util.List<Long>> userIdList() {
        return Arbitraries.longs()
                .between(1L, 1000000L)
                .list()
                .ofMinSize(1)
                .ofMaxSize(20)
                .map(list -> list.stream().distinct().toList()); // Ensure uniqueness
    }
}
