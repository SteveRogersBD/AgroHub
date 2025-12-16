package com.socialmedia.auth.service;

import com.socialmedia.auth.dto.*;
import com.socialmedia.auth.entity.RefreshToken;
import com.socialmedia.auth.entity.User;
import com.socialmedia.auth.exception.AuthenticationException;
import com.socialmedia.auth.exception.DuplicateResourceException;
import com.socialmedia.auth.exception.ResourceNotFoundException;
import com.socialmedia.auth.mapper.UserMapper;
import com.socialmedia.auth.repository.RefreshTokenRepository;
import com.socialmedia.auth.repository.UserRepository;
import com.socialmedia.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already in use");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already in use");
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create user entity
        User user = userMapper.toEntity(request, hashedPassword);

        // Save user
        user = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = createRefreshToken(user.getId());

        return userMapper.toLoginResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Find user by email or username
        User user = userRepository.findByEmail(request.getEmailOrUsername())
                .or(() -> userRepository.findByUsername(request.getEmailOrUsername()))
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        // Check if user is enabled
        if (!user.getEnabled()) {
            throw new AuthenticationException("Account is disabled");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = createRefreshToken(user.getId());

        return userMapper.toLoginResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        // Find refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        // Check if token is expired
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthenticationException("Refresh token has expired");
        }

        // Find user
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Generate new access token
        String accessToken = jwtTokenProvider.generateAccessToken(user);

        return userMapper.toLoginResponse(user, accessToken, refreshToken.getToken());
    }

    public TokenValidationResponse validateToken(TokenValidationRequest request) {
        try {
            boolean isValid = jwtTokenProvider.validateToken(request.getToken());
            
            if (!isValid) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Invalid token")
                        .build();
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(request.getToken());
            List<String> roles = jwtTokenProvider.getRolesFromToken(request.getToken());

            return TokenValidationResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .roles(roles)
                    .message("Token is valid")
                    .build();
        } catch (Exception e) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token validation failed: " + e.getMessage())
                    .build();
        }
    }

    private String createRefreshToken(Long userId) {
        // Delete existing refresh tokens for user
        refreshTokenRepository.deleteByUserId(userId);

        // Create new refresh token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiryDate(expiryDate)
                .build();

        refreshTokenRepository.save(refreshToken);

        return token;
    }
}
