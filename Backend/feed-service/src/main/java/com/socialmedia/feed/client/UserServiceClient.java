package com.socialmedia.feed.client;

import com.socialmedia.feed.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    public UserProfileResponse getUserProfile(Long userId, String token) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/api/users/user/" + userId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(UserProfileResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error fetching user profile for user {}: {}", userId, e.getMessage());
            return null;
        }
    }
}
