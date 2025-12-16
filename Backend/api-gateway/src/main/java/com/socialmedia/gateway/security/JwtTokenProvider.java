package com.socialmedia.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT token provider for validating and extracting claims from JWT tokens.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validate JWT token.
     *
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract user ID from JWT token.
     *
     * @param token JWT token
     * @return user ID
     */
    public String getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    /**
     * Extract roles from JWT token.
     *
     * @param token JWT token
     * @return list of roles
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Claims claims = getClaims(token);
        return claims.get("roles", List.class);
    }

    /**
     * Extract email from JWT token.
     *
     * @param token JWT token
     * @return email
     */
    public String getEmail(String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Get all claims from JWT token.
     *
     * @param token JWT token
     * @return claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
