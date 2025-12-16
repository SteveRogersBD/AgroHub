package com.socialmedia.follow.properties;

import com.socialmedia.follow.dto.FollowCheckResponse;
import com.socialmedia.follow.dto.FollowResponse;
import com.socialmedia.follow.dto.FollowStatsResponse;
import com.socialmedia.follow.entity.Follow;
import com.socialmedia.follow.exception.BadRequestException;
import com.socialmedia.follow.repository.FollowRepository;
import com.socialmedia.follow.service.FollowService;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-Based Tests for Follow Service
 * Feature: social-media-backend
 */
@SpringBootTest(classes = com.socialmedia.follow.FollowServiceApplication.class)
@Testcontainers
public class FollowServicePropertiesTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("follow_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;

    @BeforeEach
    void setUp() {
        followRepository.deleteAll();
    }

    /**
     * Property 20: Follow creates relationship
     * Validates: Requirements 7.1
     * 
     * For any two distinct users A and B, when A follows B, 
     * a follower relationship record should exist in the database.
     */
    @Property(tries = 100)
    void followCreatesRelationship(@ForAll("userId") Long userA,
                                   @ForAll("userId") Long userB) {
        Assume.that(!userA.equals(userB));
        
        // When A follows B
        FollowResponse response = followService.follow(userA, userB);
        
        // Then a relationship record should exist
        assertThat(response).isNotNull();
        assertThat(response.getFollowerId()).isEqualTo(userA);
        assertThat(response.getFollowingId()).isEqualTo(userB);
        
        // Verify in database
        boolean exists = followRepository.existsByFollowerIdAndFollowingId(userA, userB);
        assertThat(exists).isTrue();
    }

    /**
     * Property 21: Follow idempotence
     * Validates: Requirements 7.2
     * 
     * For any two users A and B, following B twice from A 
     * should result in exactly one follower relationship record.
     */
    @Property(tries = 100)
    void followIsIdempotent(@ForAll("userId") Long userA,
                           @ForAll("userId") Long userB) {
        Assume.that(!userA.equals(userB));
        
        // Follow twice
        followService.follow(userA, userB);
        followService.follow(userA, userB);
        
        // Should have exactly one record
        long count = followRepository.findAll().stream()
                .filter(f -> f.getFollowerId().equals(userA) && f.getFollowingId().equals(userB))
                .count();
        
        assertThat(count).isEqualTo(1);
    }

    /**
     * Property 22: Self-follow rejection
     * Validates: Requirements 7.3
     * 
     * For any user, attempting to follow themselves should be rejected with an error.
     */
    @Property(tries = 100)
    void selfFollowIsRejected(@ForAll("userId") Long userId) {
        // Attempting to follow self should throw exception
        assertThatThrownBy(() -> followService.follow(userId, userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot follow themselves");
    }

    /**
     * Property 23: Unfollow removes relationship
     * Validates: Requirements 7.4
     * 
     * For any existing follow relationship between users A and B, 
     * when A unfollows B, the relationship record should be removed from the database.
     */
    @Property(tries = 100)
    void unfollowRemovesRelationship(@ForAll("userId") Long userA,
                                    @ForAll("userId") Long userB) {
        Assume.that(!userA.equals(userB));
        
        // Create relationship
        followService.follow(userA, userB);
        assertThat(followRepository.existsByFollowerIdAndFollowingId(userA, userB)).isTrue();
        
        // Unfollow
        followService.unfollow(userA, userB);
        
        // Relationship should be removed
        boolean exists = followRepository.existsByFollowerIdAndFollowingId(userA, userB);
        assertThat(exists).isFalse();
    }

    /**
     * Property 24: Unfollow idempotence
     * Validates: Requirements 7.5
     * 
     * For any two users A and B where A does not follow B, 
     * unfollowing should complete without error.
     */
    @Property(tries = 100)
    void unfollowIsIdempotent(@ForAll("userId") Long userA,
                             @ForAll("userId") Long userB) {
        Assume.that(!userA.equals(userB));
        
        // Unfollow when not following should not throw exception
        followService.unfollow(userA, userB);
        followService.unfollow(userA, userB);
        
        // Should complete without error
        assertThat(followRepository.existsByFollowerIdAndFollowingId(userA, userB)).isFalse();
    }

    /**
     * Property 28: Follower count accuracy
     * Validates: Requirements 8.4
     * 
     * For any user, the follower count should equal the number of users who follow them, 
     * and the following count should equal the number of users they follow.
     */
    @Property(tries = 100)
    void followerCountIsAccurate(@ForAll("userId") Long targetUser,
                                 @ForAll("followerList") java.util.List<Long> followers) {
        // Create follow relationships
        for (Long follower : followers) {
            if (!follower.equals(targetUser)) {
                followService.follow(follower, targetUser);
            }
        }
        
        // Get stats
        FollowStatsResponse stats = followService.getFollowStats(targetUser);
        
        // Count should match number of distinct followers (excluding self)
        long expectedFollowerCount = followers.stream()
                .filter(f -> !f.equals(targetUser))
                .distinct()
                .count();
        
        assertThat(stats.getFollowerCount()).isEqualTo(expectedFollowerCount);
    }

    @Provide
    Arbitrary<Long> userId() {
        return Arbitraries.longs().between(1L, 10000L);
    }

    @Provide
    Arbitrary<java.util.List<Long>> followerList() {
        return Arbitraries.longs().between(1L, 100L)
                .list().ofMinSize(0).ofMaxSize(10);
    }
}
