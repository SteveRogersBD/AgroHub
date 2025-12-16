package com.socialmedia.feed.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.like-service.url:http://localhost:8086}")
    private String likeServiceUrl;

    public Long getLikeCount(Long postId, String token) {
        try {
            var response = webClientBuilder.build()
                    .get()
                    .uri(likeServiceUrl + "/api/likes/" + postId + "/count")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(LikeCountResponse.class)
                    .block();
            
            return response != null ? response.getCount() : 0L;
        } catch (Exception e) {
            log.error("Error fetching like count for post {}: {}", postId, e.getMessage());
            return 0L;
        }
    }

    public Boolean checkIfUserLiked(Long postId, String token) {
        try {
            var response = webClientBuilder.build()
                    .get()
                    .uri(likeServiceUrl + "/api/likes/" + postId + "/check")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(LikeCheckResponse.class)
                    .block();
            
            return response != null ? response.getLiked() : false;
        } catch (Exception e) {
            log.error("Error checking if user liked post {}: {}", postId, e.getMessage());
            return false;
        }
    }

    public Map<Long, Long> getBatchLikeCounts(List<Long> postIds, String token) {
        try {
            var request = new BatchLikeCountRequest(postIds);
            var response = webClientBuilder.build()
                    .post()
                    .uri(likeServiceUrl + "/api/likes/batch/counts")
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(BatchLikeCountResponse.class)
                    .block();
            
            return response != null ? response.getCounts() : Map.of();
        } catch (Exception e) {
            log.error("Error fetching batch like counts: {}", e.getMessage());
            return postIds.stream().collect(Collectors.toMap(id -> id, id -> 0L));
        }
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class LikeCountResponse {
        private Long postId;
        private Long count;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class LikeCheckResponse {
        private Long postId;
        private Boolean liked;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BatchLikeCountRequest {
        private List<Long> postIds;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class BatchLikeCountResponse {
        private Map<Long, Long> counts;
    }
}
