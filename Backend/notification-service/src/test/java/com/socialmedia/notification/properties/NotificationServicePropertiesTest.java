package com.socialmedia.notification.properties;

import com.socialmedia.notification.dto.CreateNotificationRequest;
import com.socialmedia.notification.dto.NotificationListResponse;
import com.socialmedia.notification.dto.NotificationResponse;
import com.socialmedia.notification.entity.Notification;
import com.socialmedia.notification.exception.BadRequestException;
import com.socialmedia.notification.repository.NotificationRepository;
import com.socialmedia.notification.service.NotificationService;
import net.jqwik.api.*;
import net.jqwik.spring.JqwikSpringSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@JqwikSpringSupport
@SpringBootTest(classes = com.socialmedia.notification.NotificationServiceApplication.class)
@Testcontainers
class NotificationServicePropertiesTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("notification_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    // Feature: social-media-backend, Property 60: Like creates notification for post author
    // Validates: Requirements 18.1
    @Property(tries = 100)
    @Label("likeShouldCreateNotificationForPostAuthor")
    void likeShouldCreateNotificationForPostAuthor(
            @ForAll("validUserId") Long postAuthorId,
            @ForAll("validUserId") Long likerId,
            @ForAll("validPostId") Long postId) {
        
        Assume.that(!postAuthorId.equals(likerId)); // Different users

        // When another user likes a user's post
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(postAuthorId)
                .type("LIKE")
                .actorId(likerId)
                .entityId(postId)
                .message("User " + likerId + " liked your post")
                .build();

        NotificationResponse response = notificationService.createNotification(request);

        // Then a notification should be created for the post author
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getUserId()).isEqualTo(postAuthorId);
        Assertions.assertThat(response.getActorId()).isEqualTo(likerId);
        Assertions.assertThat(response.getEntityId()).isEqualTo(postId);
        Assertions.assertThat(response.getType()).isEqualTo("LIKE");

        // Verify in database
        List<Notification> notifications = notificationRepository.findAll();
        Assertions.assertThat(notifications).hasSize(1);
        Assertions.assertThat(notifications.get(0).getUserId()).isEqualTo(postAuthorId);
        Assertions.assertThat(notifications.get(0).getActorId()).isEqualTo(likerId);
    }

    // Feature: social-media-backend, Property 63: Self-actions do not create notifications
    // Validates: Requirements 18.4
    @Property(tries = 100)
    @Label("selfActionsShouldNotCreateNotifications")
    void selfActionsShouldNotCreateNotifications(
            @ForAll("validUserId") Long userId,
            @ForAll("validPostId") Long postId) {
        
        // When a user performs an action on their own content
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(userId)
                .type("LIKE")
                .actorId(userId) // Same user
                .entityId(postId)
                .message("Self-action")
                .build();

        // Then the notification creation should be rejected
        Assertions.assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot create notification for self-action");

        // Verify no notification was created in database
        List<Notification> notifications = notificationRepository.findAll();
        Assertions.assertThat(notifications).isEmpty();
    }

    // Feature: social-media-backend, Property 64: New notifications are unread by default
    // Validates: Requirements 18.5
    @Property(tries = 100)
    @Label("newNotificationsShouldBeUnreadByDefault")
    void newNotificationsShouldBeUnreadByDefault(
            @ForAll("validUserId") Long userId,
            @ForAll("validUserId") Long actorId,
            @ForAll("validPostId") Long postId) {
        
        Assume.that(!userId.equals(actorId)); // Different users

        // When a notification is created
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(userId)
                .type("COMMENT")
                .actorId(actorId)
                .entityId(postId)
                .message("User " + actorId + " commented on your post")
                .build();

        NotificationResponse response = notificationService.createNotification(request);

        // Then the notification should be unread by default
        Assertions.assertThat(response.getRead()).isFalse();

        // Verify in database
        Notification savedNotification = notificationRepository.findById(response.getId()).orElseThrow();
        Assertions.assertThat(savedNotification.getRead()).isFalse();
    }

    // Feature: social-media-backend, Property 66: Unread filter returns only unread notifications
    // Validates: Requirements 19.2
    @Property(tries = 100)
    @Label("unreadFilterShouldReturnOnlyUnreadNotifications")
    void unreadFilterShouldReturnOnlyUnreadNotifications(
            @ForAll("validUserId") Long userId,
            @ForAll("notificationCount") int totalCount) {
        
        Assume.that(totalCount >= 2);

        // Create multiple notifications, mark some as read
        for (int i = 0; i < totalCount; i++) {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .userId(userId)
                    .type("LIKE")
                    .actorId(userId + i + 1) // Different actor
                    .entityId((long) (i + 1))
                    .message("Notification " + i)
                    .build();

            NotificationResponse notification = notificationService.createNotification(request);

            // Mark half of them as read
            if (i % 2 == 0) {
                notificationService.markAsRead(notification.getId(), userId);
            }
        }

        // When getting unread notifications
        NotificationListResponse unreadResponse = notificationService.getUnreadNotifications(userId, 0, 100);

        // Then only unread notifications should be returned
        int expectedUnreadCount = (totalCount + 1) / 2; // Ceiling division for odd numbers
        Assertions.assertThat(unreadResponse.getNotifications()).hasSize(expectedUnreadCount);
        
        // All returned notifications should be unread
        for (NotificationResponse notification : unreadResponse.getNotifications()) {
            Assertions.assertThat(notification.getRead()).isFalse();
        }

        // Verify in database
        List<Notification> allNotifications = notificationRepository.findAll();
        long unreadCount = allNotifications.stream().filter(n -> !n.getRead()).count();
        Assertions.assertThat(unreadCount).isEqualTo(expectedUnreadCount);
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
    Arbitrary<Integer> notificationCount() {
        return Arbitraries.integers().between(2, 10);
    }
}
