package com.socialmedia.auth.integration;

import com.socialmedia.auth.dto.*;
import com.socialmedia.auth.repository.RefreshTokenRepository;
import com.socialmedia.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("auth_integration_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCompleteRegistrationFlow() {
        // Given a registration request
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .build();

        // When registering
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/register",
                request,
                LoginResponse.class
        );

        // Then registration should succeed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotEmpty();
        assertThat(response.getBody().getRefreshToken()).isNotEmpty();
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getRole()).isEqualTo("USER");
    }

    @Test
    void shouldLoginWithValidCredentials() {
        // Given a registered user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("login@example.com")
                .username("loginuser")
                .password("password123")
                .build();
        restTemplate.postForEntity("/api/auth/register", registerRequest, LoginResponse.class);

        // When logging in with valid credentials
        LoginRequest loginRequest = LoginRequest.builder()
                .emailOrUsername("login@example.com")
                .password("password123")
                .build();

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                LoginResponse.class
        );

        // Then login should succeed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotEmpty();
        assertThat(response.getBody().getRefreshToken()).isNotEmpty();
    }

    @Test
    void shouldRejectLoginWithInvalidCredentials() {
        // Given a registered user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("invalid@example.com")
                .username("invaliduser")
                .password("password123")
                .build();
        restTemplate.postForEntity("/api/auth/register", registerRequest, LoginResponse.class);

        // When logging in with invalid password
        LoginRequest loginRequest = LoginRequest.builder()
                .emailOrUsername("invalid@example.com")
                .password("wrongpassword")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                String.class
        );

        // Then login should fail
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        // Given a registered user with tokens
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("refresh@example.com")
                .username("refreshuser")
                .password("password123")
                .build();
        ResponseEntity<LoginResponse> registerResponse = restTemplate.postForEntity(
                "/api/auth/register",
                registerRequest,
                LoginResponse.class
        );

        String refreshToken = registerResponse.getBody().getRefreshToken();

        // When refreshing the token
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/refresh",
                refreshRequest,
                LoginResponse.class
        );

        // Then refresh should succeed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotEmpty();
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // Given a registered user with access token
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("validate@example.com")
                .username("validateuser")
                .password("password123")
                .build();
        ResponseEntity<LoginResponse> registerResponse = restTemplate.postForEntity(
                "/api/auth/register",
                registerRequest,
                LoginResponse.class
        );

        String accessToken = registerResponse.getBody().getAccessToken();

        // When validating the token
        TokenValidationRequest validationRequest = TokenValidationRequest.builder()
                .token(accessToken)
                .build();

        ResponseEntity<TokenValidationResponse> response = restTemplate.postForEntity(
                "/api/auth/validate",
                validationRequest,
                TokenValidationResponse.class
        );

        // Then validation should succeed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isValid()).isTrue();
        assertThat(response.getBody().getUserId()).isNotNull();
        assertThat(response.getBody().getRoles()).contains("USER");
    }
}
