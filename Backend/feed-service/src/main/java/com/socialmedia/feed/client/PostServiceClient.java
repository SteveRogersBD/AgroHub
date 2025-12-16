package com.socialmedia.feed.client;

import com.socialmedia.feed.dto.PostListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.post-service.url:http://localhost:8084}")
    private String postServiceUrl;

    public PostListResponse getPostsByUser(Long userId, int page, int size, String token) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(postServiceUrl + "/api/posts/user/" + userId + "?page=" + page + "&size=" + size)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(PostListResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error fetching posts for user {}: {}", userId, e.getMessage());
            return PostListResponse.builder()
                    .posts(java.util.List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .build();
        }
    }
}
