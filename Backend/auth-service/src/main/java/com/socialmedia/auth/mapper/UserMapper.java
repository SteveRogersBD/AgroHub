package com.socialmedia.auth.mapper;

import com.socialmedia.auth.dto.LoginResponse;
import com.socialmedia.auth.dto.RegisterRequest;
import com.socialmedia.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request, String hashedPassword) {
        return User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(hashedPassword)
                .role("USER")
                .enabled(true)
                .build();
    }

    public LoginResponse toLoginResponse(User user, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
