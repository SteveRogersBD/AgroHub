package com.socialmedia.follow.integration;

import com.socialmedia.follow.dto.*;
import com.socialmedia.follow.repository.FollowRepository;
import com.socialmedia.follow.service.FollowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class FollowServiceIntegrationTest {

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

    @Test
    void testFollowUnfollowOperations() {
        // Given two users
        Long userA = 1L;
        Long userB = 2L;

        // When userA follows userB
        FollowResponse followResponse = followService.follow(userA, userB);

        // Then the relationship is created
        assertThat(followResponse).isNotNull();
        assertThat(followResponse.getFollowerId()).isEqualTo(userA);
        assertThat(followResponse.getFollowingId()).isEqualTo(userB);
        assertThat(followRepository.existsByFollowerIdAndFollowingId(userA, userB)).isTrue();

        // When userA unfollows userB
        followService.unfollow(userA, userB);

        // Then the relationship is removed
        assertThat(followRepository.existsByFollowerIdAndFollowingId(userA, userB)).isFalse();
    }

    @Test
    void testFollowIdempotence() {
        // Given two users
        Long userA = 1L;
        Long userB = 2L;

        // When userA follows userB twice
        followService.follow(userA, userB);
        followService.follow(userA, userB);

        // Then only one relationship exists
        long count = followRepository.findAll().size();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testUnfollowIdempotence() {
        // Given two users where A does not follow B
        Long userA = 1L;
        Long userB = 2L;

        // When userA unfollows userB (without following first)
        followService.unfollow(userA, userB);
        followService.unfollow(userA, userB);

        // Then no error occurs
        assertThat(followRepository.existsByFollowerIdAndFollowingId(userA, userB)).isFalse();
    }

    @Test
    void testSelfFollowRejection() {
        // Given a user
        Long userId = 1L;

        // When the user tries to follow themselves
        // Then an exception is thrown
        assertThatThrownBy(() -> followService.follow(userId, userId))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("cannot follow themselves");
    }

    @Test
    void testGetFollowersWithPagination() {
        // Given a user with multiple followers
        Long targetUser = 1L;
        Long follower1 = 2L;
        Long follower2 = 3L;
        Long follower3 = 4L;

        followService.follow(follower1, targetUser);
        followService.follow(follower2, targetUser);
        followService.follow(follower3, targetUser);

        // When getting followers with pagination
        FollowerListResponse response = followService.getFollowers(targetUser, 0, 2);

        // Then the correct page is returned
        assertThat(response.getFollowerIds()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testGetFollowingWithPagination() {
        // Given a user following multiple users
        Long follower = 1L;
        Long following1 = 2L;
        Long following2 = 3L;
        Long following3 = 4L;

        followService.follow(follower, following1);
        followService.follow(follower, following2);
        followService.follow(follower, following3);

        // When getting following with pagination
        FollowingListResponse response = followService.getFollowing(follower, 0, 2);

        // Then the correct page is returned
        assertThat(response.getFollowingIds()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testFollowerCounts() {
        // Given a user with followers and following
        Long user = 1L;
        Long follower1 = 2L;
        Long follower2 = 3L;
        Long following1 = 4L;
        Long following2 = 5L;

        followService.follow(follower1, user);
        followService.follow(follower2, user);
        followService.follow(user, following1);
        followService.follow(user, following2);

        // When getting follow stats
        FollowStatsResponse stats = followService.getFollowStats(user);

        // Then the counts are accurate
        assertThat(stats.getFollowerCount()).isEqualTo(2);
        assertThat(stats.getFollowingCount()).isEqualTo(2);
    }

    @Test
    void testCheckIfFollowing() {
        // Given two users where A follows B
        Long userA = 1L;
        Long userB = 2L;
        followService.follow(userA, userB);

        // When checking if A follows B
        FollowCheckResponse response = followService.checkIfFollowing(userA, userB);

        // Then the result is true
        assertThat(response.isFollowing()).isTrue();

        // When checking if B follows A
        FollowCheckResponse response2 = followService.checkIfFollowing(userB, userA);

        // Then the result is false
        assertThat(response2.isFollowing()).isFalse();
    }
}
