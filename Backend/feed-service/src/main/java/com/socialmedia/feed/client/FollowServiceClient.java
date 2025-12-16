package com.socialmedia.feed.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FollowServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.follow-service.url:http://localhost:8083}")
    private String followServiceUrl;

    public List<Long> getFollowingIds(Long userId, String token) {
        try {
            var response = webClientBuilder.build()
                    .get()
                    .uri(followServiceUrl + "/api/follows/" + userId + "/following?page=0&size=1000")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(FollowingListResponse.class)
                    .block();
            
            return response != null ? response.getFollowingIds() : List.of();
        } catch (Exception e) {
            log.error("Error fetching following list for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class FollowingListResponse {
        private List<Long> followingIds;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
