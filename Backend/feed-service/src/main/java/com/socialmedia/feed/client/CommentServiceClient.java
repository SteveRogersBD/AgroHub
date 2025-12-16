package com.socialmedia.feed.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.comment-service.url:http://localhost:8085}")
    private String commentServiceUrl;

    public Long getCommentCount(Long postId, String token) {
        try {
            var response = webClientBuilder.build()
                    .get()
                    .uri(commentServiceUrl + "/api/comments/post/" + postId + "?page=0&size=1")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(CommentListResponse.class)
                    .block();
            
            return response != null ? response.getTotalElements() : 0L;
        } catch (Exception e) {
            log.error("Error fetching comment count for post {}: {}", postId, e.getMessage());
            return 0L;
        }
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class CommentListResponse {
        private java.util.List<Object> comments;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
