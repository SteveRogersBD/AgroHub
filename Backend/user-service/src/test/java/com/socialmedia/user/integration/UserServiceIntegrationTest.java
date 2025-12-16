package com.socialmedia.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmedia.user.dto.UserProfileRequest;
import com.socialmedia.user.dto.UserProfileResponse;
import com.socialmedia.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_integration_test_db")
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
    private UserProfileRepository userProfileRepository;

    @BeforeEach
    void setUp() {
        userProfileRepository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveProfile() throws Exception {
        // Given
        Long userId = 1L;
        
        UserProfileRequest request = UserProfileRequest.builder()
                .name("John Doe")
                .bio("Software Developer")
                .avatarUrl("https://example.com/avatar.jpg")
                .location("New York")
                .website("https://johndoe.com")
                .build();

        // When - Create profile
        String createResponse = mockMvc.perform(post("/api/users")
                .header("X-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.bio").value("Software Developer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserProfileResponse createdProfile = objectMapper.readValue(createResponse, UserProfileResponse.class);

        // Then - Retrieve profile
        mockMvc.perform(get("/api/users/" + createdProfile.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProfile.getId()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.bio").value("Software Developer"));
    }

    @Test
    void shouldUpdateOwnProfile() throws Exception {
        // Given - Create a profile
        Long userId = 2L;
        
        UserProfileRequest createRequest = UserProfileRequest.builder()
                .name("Jane Smith")
                .bio("Designer")
                .build();

        String createResponse = mockMvc.perform(post("/api/users")
                .header("X-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserProfileResponse createdProfile = objectMapper.readValue(createResponse, UserProfileResponse.class);

        // When - Update profile
        UserProfileRequest updateRequest = UserProfileRequest.builder()
                .name("Jane Smith Updated")
                .bio("Senior Designer")
                .location("San Francisco")
                .build();

        mockMvc.perform(put("/api/users/" + createdProfile.getId())
                .header("X-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Smith Updated"))
                .andExpect(jsonPath("$.bio").value("Senior Designer"))
                .andExpect(jsonPath("$.location").value("San Francisco"));
    }

    @Test
    void shouldRejectCrossUserProfileUpdate() throws Exception {
        // Given - User 1 creates a profile
        Long userId1 = 3L;
        
        UserProfileRequest request = UserProfileRequest.builder()
                .name("User One")
                .build();

        String createResponse = mockMvc.perform(post("/api/users")
                .header("X-User-Id", userId1.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserProfileResponse profile = objectMapper.readValue(createResponse, UserProfileResponse.class);

        // When - User 2 tries to update User 1's profile
        Long userId2 = 4L;
        
        UserProfileRequest updateRequest = UserProfileRequest.builder()
                .name("Hacked Name")
                .build();

        // Then - Should be forbidden
        mockMvc.perform(put("/api/users/" + profile.getId())
                .header("X-User-Id", userId2.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("AUTHORIZATION_ERROR"));
    }

    @Test
    void shouldSearchUsersByName() throws Exception {
        // Given - Create multiple profiles
        Long userId1 = 5L;
        Long userId2 = 6L;
        Long userId3 = 7L;

        mockMvc.perform(post("/api/users")
                .header("X-User-Id", userId1.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        UserProfileRequest.builder().name("Alice Johnson").build())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                .header("X-User-Id", userId2.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        UserProfileRequest.builder().name("Bob Smith").build())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                .header("X-User-Id", userId3.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        UserProfileRequest.builder().name("Alice Brown").build())))
                .andExpect(status().isCreated());

        // When - Search for "Alice"
        mockMvc.perform(get("/api/users/search")
                .param("name", "Alice")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldGetCurrentUserProfile() throws Exception {
        // Given - Create a profile
        Long userId = 8L;
        
        UserProfileRequest request = UserProfileRequest.builder()
                .name("Current User")
                .bio("This is me")
                .build();

        mockMvc.perform(post("/api/users")
                .header("X-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // When - Get current user profile
        mockMvc.perform(get("/api/users/me")
                .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.name").value("Current User"))
                .andExpect(jsonPath("$.bio").value("This is me"));
    }

    @Test
    void shouldHandlePaginationCorrectly() throws Exception {
        // Given - Create 15 profiles
        for (int i = 1; i <= 15; i++) {
            Long userId = (long) (100 + i);
            
            mockMvc.perform(post("/api/users")
                    .header("X-User-Id", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            UserProfileRequest.builder().name("User " + i).build())))
                    .andExpect(status().isCreated());
        }

        // When - Request first page
        mockMvc.perform(get("/api/users/search")
                .param("name", "")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(5))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.totalElements").value(15));

        // When - Request second page
        mockMvc.perform(get("/api/users/search")
                .param("name", "")
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(5))
                .andExpect(jsonPath("$.currentPage").value(1));
    }
}
