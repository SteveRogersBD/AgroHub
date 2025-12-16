package com.socialmedia.gateway.properties;

import com.socialmedia.gateway.security.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * Property-based tests for JWT Token Provider.
 * Tests token validation and claim extraction logic.
 */
class JwtTokenProviderPropertiesTest {

    private static final String JWT_SECRET = "test-secret-key-for-jwt-token-validation-must-be-256-bits-long";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(JWT_SECRET);

    /**
     * Feature: social-media-backend, Property 70: Gateway validates JWT for protected endpoints
     * For any valid JWT token, the token provider should validate it successfully.
     * Validates: Requirements 20.2
     */
    @Property(tries = 100)
    void validTokensShouldPassValidation(@ForAll("validUserIds") String userId,
                                         @ForAll("validEmails") String email,
                                         @ForAll("validRoles") List<String> roles) {
        String token = generateValidToken(userId, email, roles);
        
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        assert isValid : "Valid token should pass validation";
    }

    /**
     * Feature: social-media-backend, Property 71: Gateway rejects invalid JWT
     * For any invalid JWT token, the token provider should reject it.
     * Validates: Requirements 20.3
     */
    @Property(tries = 100)
    void invalidTokensShouldFailValidation(@ForAll("invalidTokens") String invalidToken) {
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        assert !isValid : "Invalid token should fail validation";
    }

    /**
     * Test that expired tokens are rejected.
     */
    @Property(tries = 100)
    void expiredTokensShouldFailValidation(@ForAll("validUserIds") String userId,
                                           @ForAll("validEmails") String email,
                                           @ForAll("validRoles") List<String> roles) {
        String expiredToken = generateExpiredToken(userId, email, roles);
        
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);
        
        assert !isValid : "Expired token should fail validation";
    }

    /**
     * Test that user ID can be extracted from valid tokens.
     */
    @Property(tries = 100)
    void shouldExtractUserIdFromValidToken(@ForAll("validUserIds") String userId,
                                           @ForAll("validEmails") String email,
                                           @ForAll("validRoles") List<String> roles) {
        String token = generateValidToken(userId, email, roles);
        
        String extractedUserId = jwtTokenProvider.getUserId(token);
        
        assert extractedUserId.equals(userId) : 
            "Extracted user ID should match original: expected " + userId + " but got " + extractedUserId;
    }

    /**
     * Test that email can be extracted from valid tokens.
     */
    @Property(tries = 100)
    void shouldExtractEmailFromValidToken(@ForAll("validUserIds") String userId,
                                          @ForAll("validEmails") String email,
                                          @ForAll("validRoles") List<String> roles) {
        String token = generateValidToken(userId, email, roles);
        
        String extractedEmail = jwtTokenProvider.getEmail(token);
        
        assert extractedEmail.equals(email) : 
            "Extracted email should match original: expected " + email + " but got " + extractedEmail;
    }

    /**
     * Test that roles can be extracted from valid tokens.
     */
    @Property(tries = 100)
    void shouldExtractRolesFromValidToken(@ForAll("validUserIds") String userId,
                                          @ForAll("validEmails") String email,
                                          @ForAll("validRoles") List<String> roles) {
        String token = generateValidToken(userId, email, roles);
        
        List<String> extractedRoles = jwtTokenProvider.getRoles(token);
        
        assert extractedRoles.equals(roles) : 
            "Extracted roles should match original: expected " + roles + " but got " + extractedRoles;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<String> validUserIds() {
        return Arbitraries.integers().between(1, 1000000).map(String::valueOf);
    }

    @Provide
    Arbitrary<String> validEmails() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(10)
                .map(name -> name + "@example.com");
    }

    @Provide
    Arbitrary<List<String>> validRoles() {
        return Arbitraries.of("USER", "ADMIN", "MODERATOR")
                .list()
                .ofMinSize(1)
                .ofMaxSize(3);
    }

    @Provide
    Arbitrary<String> invalidTokens() {
        return Arbitraries.of(
                "invalid-token",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature",
                "",
                "malformed.token",
                "Bearer token",
                "not-a-jwt"
        );
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
