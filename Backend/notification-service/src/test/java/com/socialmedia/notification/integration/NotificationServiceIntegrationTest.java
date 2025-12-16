package com.socialmedia.notification.integration;

import com.socialmedia.notification.dto.CreateNotificationRequest;
import com.socialmedia.notification.dto.NotificationListResponse;
import com.socialmedia.notification.dto.NotificationResponse;
import com.socialmedia.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class NotificationServiceIntegrationTest {

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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        // Mock JWT token for testing
        jwtToken = "Bearer mock-jwt-token";
    }

    @Test
    void shouldCreateNotification() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(1L)
                .type("LIKE")
                .actorId(2L)
                .entityId(100L)
                .message("User 2 liked your post")
                .build();

        MvcResult result = mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.type").value("LIKE"))
                .andExpect(jsonPath("$.actorId").value(2))
                .andExpect(jsonPath("$.read").value(false))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        NotificationResponse response = objectMapper.readValue(responseBody, NotificationResponse.class);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectSelfNotification() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(1L)
                .type("LIKE")
                .actorId(1L) // Same as userId
                .entityId(100L)
                .message("Self-action")
                .build();

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot create notification for self-action"));
    }

    @Test
    void shouldGetNotificationsWithPagination() throws Exception {
        // Create multiple notifications
        for (int i = 0; i < 5; i++) {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .userId(1L)
                    .type("LIKE")
                    .actorId((long) (i + 2))
                    .entityId((long) (i + 100))
                    .message("Notification " + i)
                    .build();

            mockMvc.perform(post("/api/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Get notifications with pagination - mock authentication
        MvcResult result = mockMvc.perform(get("/api/notifications")
                        .param("page", "0")
                        .param("size", "3")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        NotificationListResponse response = objectMapper.readValue(responseBody, NotificationListResponse.class);

        assertThat(response.getNotifications()).hasSize(3);
        assertThat(response.getTotalItems()).isEqualTo(5);
        assertThat(response.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldGetUnreadNotifications() throws Exception {
        // Create notifications
        CreateNotificationRequest request1 = CreateNotificationRequest.builder()
                .userId(1L)
                .type("LIKE")
                .actorId(2L)
                .entityId(100L)
                .message("Notification 1")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andReturn();

        NotificationResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                NotificationResponse.class
        );

        // Mark one as read
        mockMvc.perform(put("/api/notifications/" + created.getId() + "/read")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());

        // Create another unread notification
        CreateNotificationRequest request2 = CreateNotificationRequest.builder()
                .userId(1L)
                .type("COMMENT")
                .actorId(3L)
                .entityId(101L)
                .message("Notification 2")
                .build();

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // Get unread notifications
        MvcResult result = mockMvc.perform(get("/api/notifications/unread")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        NotificationListResponse response = objectMapper.readValue(responseBody, NotificationListResponse.class);

        assertThat(response.getNotifications()).hasSize(1);
        assertThat(response.getNotifications().get(0).getRead()).isFalse();
    }

    @Test
    void shouldMarkNotificationAsRead() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(1L)
                .type("LIKE")
                .actorId(2L)
                .entityId(100L)
                .message("Test notification")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        NotificationResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                NotificationResponse.class
        );

        assertThat(created.getRead()).isFalse();

        // Mark as read
        mockMvc.perform(put("/api/notifications/" + created.getId() + "/read")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void shouldMarkAllNotificationsAsRead() throws Exception {
        // Create multiple notifications
        for (int i = 0; i < 3; i++) {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .userId(1L)
                    .type("LIKE")
                    .actorId((long) (i + 2))
                    .entityId((long) (i + 100))
                    .message("Notification " + i)
                    .build();

            mockMvc.perform(post("/api/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Mark all as read
        mockMvc.perform(put("/api/notifications/read-all")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());

        // Verify all are read
        MvcResult result = mockMvc.perform(get("/api/notifications/unread")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        NotificationListResponse response = objectMapper.readValue(responseBody, NotificationListResponse.class);

        assertThat(response.getNotifications()).isEmpty();
    }
}
