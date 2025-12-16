package com.socialmedia.gateway.integration;

import com.socialmedia.gateway.security.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for API Gateway.
 * Tests routing, JWT authentication, and error handling.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String JWT_SECRET = "test-secret-key-for-jwt-token-validation-must-be-256-bits-long";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> JWT_SECRET);
        // Configure backend service URLs to non-existent ports to simulate unavailable services
        registry.add("services.auth-service.url", () -> "http://localhost:9999");
        registry.add("services.user-service.url", () -> "http://localhost:9999");
        registry.add("services.follow-service.url", () -> "http://localhost:9999");
        registry.add("services.post-service.url", () -> "http://localhost:9999");
        registry.add("services.comment-service.url", () -> "http://localhost:9999");
        registry.add("services.like-service.url", () -> "http://localhost:9999");
        registry.add("services.feed-service.url", () -> "http://localhost:9999");
        registry.add("services.notification-service.url", () -> "http://localhost:9999");
    }

    /**
     * Test that gateway routes requests to correct services.
     * Validates: Requirements 20.1
     */
    @Test
    void testGatewayRoutesToCorrectService() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        String token = generateValidToken("1", "test@example.com", List.of("USER"));

        // Test routing to user service
        Mono<org.springframework.web.reactive.function.client.ClientResponse> responseMono = webClient
                .get()
                .uri("/api/users/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange();

        org.springframework.web.reactive.function.client.ClientResponse response = responseMono.block();
        assertNotNull(response);
        
        // Since backend service is not running, we expect SERVICE_UNAVAILABLE or similar
        HttpStatus status = (HttpStatus) response.statusCode();
        assertTrue(status == HttpStatus.SERVICE_UNAVAILABLE || 
                  status == HttpStatus.GATEWAY_TIMEOUT ||
                  status == HttpStatus.BAD_GATEWAY,
                "Expected gateway to attempt routing (503/504/502), but got: " + status);
    }

    /**
     * Test that gateway validates JWT for protected endpoints.
     * Validates: Requirements 20.2
     */
    @Test
    void testGatewayValidatesJwtForProtectedEndpoints() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        String token = generateValidToken("1", "test@example.com", List.of("USER"));

        // Make request with valid token
        Mono<org.springframework.web.reactive.function.client.ClientResponse> responseMono = webClient
                .get()
                .uri("/api/users/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange();

        org.springframework.web.reactive.function.client.ClientResponse response = responseMono.block();
        assertNotNull(response);
        
        HttpStatus status = (HttpStatus) response.statusCode();
        
        // With a valid token, we should NOT get 401 Unauthorized
        assertNotEquals(HttpStatus.UNAUTHORIZED, status,
                "Valid JWT token should not result in 401 Unauthorized");
    }

    /**
     * Test that gateway rejects invalid JWT tokens.
     * Validates: Requirements 20.3
     */
    @Test
    void testGatewayRejectsInvalidJwt() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        // Make request with invalid token
        Mono<org.springframework.web.reactive.function.client.ClientResponse> responseMono = webClient
                .get()
                .uri("/api/users/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .exchange();

        org.springframework.web.reactive.function.client.ClientResponse response = responseMono.block();
        assertNotNull(response);
        
        HttpStatus status = (HttpStatus) response.statusCode();
        
        // Invalid token should result in 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, status,
                "Invalid JWT token should result in 401 Unauthorized");
    }

    /**
     * Test that gateway rejects expired JWT tokens.
     */
    @Test
    void testGatewayRejectsExpiredJwt() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        String expiredToken = generateExpiredToken("1", "test@example.com", List.of("USER"));

        // Make request with expired token
        Mono<org.springframework.web.reactive.function.client.ClientResponse> responseMono = webClient
                .get()
                .uri("/api/users/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .exchange();

        org.springframework.web.reactive.function.client.ClientResponse response = responseMono.block();
        assertNotNull(response);
        
        HttpStatus status = (HttpStatus) response.statusCode();
        
        // Expired token should result in 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, status,
                "Expired JWT token should result in 401 Unauthorized");
    }

    /**
     * Test that gateway allows unauthenticated access to auth endpoints.
     */
    @Test
    void testGatewayAllowsUnauthenticatedAuthEndpoints() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        // Make request to login endpoint without token
        Mono<org.springframework.web.reactive.function.client.ClientResponse> responseMono = webClient
                .post()
                .uri("/api/auth/login")
                .exchange();

        org.springframework.web.reactive.function.client.ClientResponse response = responseMono.block();
        assertNotNull(response);
        
        HttpStatus status = (HttpStatus) response.statusCode();
        
        // Should NOT get 401 for auth endpoints
        assertNotEquals(HttpStatus.UNAUTHORIZED, status,
                "Auth endpoints should not require authentication");
    }

    /**
     * Test that gateway rejects requests without Authorization header.
     */
    @Test
    void testGatewayRejectsMissingAuthorizationHeader() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        // Make request without Authorization header
        Mono<org.springframework.web.reactive.function.client.ClientResponse> responseMono = webClient
                .get()
                .uri("/api/users/1")
                .exchange();

        org.springframework.web.reactive.function.client.ClientResponse response = responseMono.block();
        assertNotNull(response);
        
        HttpStatus status = (HttpStatus) response.statusCode();
        
        // Missing auth header should result in 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, status,
                "Missing Authorization header should result in 401 Unauthorized");
    }

    // ==================== Helper Methods ====================

    private String generateValidToken(String userId, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant expiration = now.plus(1, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(SECRET_KEY)
                .compact();
    }

    private String generateExpiredToken(String userId, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant expiration = now.minus(1, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(Date.from(now.minus(2, ChronoUnit.HOURS)))
                .expiration(Date.from(expiration))
                .signWith(SECRET_KEY)
                .compact();
    }
}
