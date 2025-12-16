package com.socialmedia.feed.integration;

import com.socialmedia.feed.FeedServiceApplication;
import com.socialmedia.feed.client.CommentServiceClient;
import com.socialmedia.feed.client.FollowServiceClient;
import com.socialmedia.feed.client.LikeServiceClient;
import com.socialmedia.feed.client.PostServiceClient;
import com.socialmedia.feed.dto.FeedResponse;
import com.socialmedia.feed.dto.PostListResponse;
import com.socialmedia.feed.dto.PostResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Integration Tests for Feed Service
 * Tests the complete feed generation flow with mocked service responses
 */
@SpringBootTest(
    classes = FeedServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class FeedServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FollowServiceClient followServiceClient;

    @MockBean
    private PostServiceClient postServiceClient;

    @MockBean
    private LikeServiceClient likeServiceClient;

    @MockBean
    private CommentServiceClient commentServiceClient;

    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
    }

    @Test
    void testGetFeed_WithFollowedUsers_ReturnsEnrichedPosts() {
        // Given: User 1 follows users 2 and 3
        Long currentUserId = 1L;
        List<Long> followingIds = List.of(2L, 3L);
        
        when(followServiceClient.getFollowingIds(eq(currentUserId), anyString()))
                .thenReturn(followingIds);
        
        // Given: Posts from followed users
        PostResponse post1 = PostResponse.builder()
                .id(101L)
                .userId(2L)
                .content("Post from user 2")
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
        
        PostResponse post2 = PostResponse.builder()
                .id(102L)
                .userId(3L)
                .content("Post from user 3")
                .createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(LocalDateTime.now().minusHours(2))
                .build();
        
        when(postServiceClient.getPostsByUser(eq(2L), anyInt(), anyInt(), anyString()))
                .thenReturn(PostListResponse.builder()
                        .posts(List.of(post1))
                        .page(0)
                        .size(100)
                        .totalElements(1)
                        .totalPages(1)
                        .build());
        
        when(postServiceClient.getPostsByUser(eq(3L), anyInt(), anyInt(), anyString()))
                .thenReturn(PostListResponse.builder()
                        .posts(List.of(post2))
                        .page(0)
                        .size(100)
                        .totalElements(1)
                        .totalPages(1)
                        .build());
        
        // Given: Metadata for posts
        Map<Long, Long> likeCounts = new HashMap<>();
        likeCounts.put(101L, 5L);
        likeCounts.put(102L, 3L);
        
        when(likeServiceClient.getBatchLikeCounts(anyList(), anyString()))
                .thenReturn(likeCounts);
        when(likeServiceClient.checkIfUserLiked(anyLong(), anyString()))
                .thenReturn(false);
        when(commentServiceClient.getCommentCount(anyLong(), anyString()))
                .thenReturn(2L);
        
        // When: Request feed
        ResponseEntity<FeedResponse> response = restTemplate.exchange(
                "/api/feed?page=0&size=20",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                FeedResponse.class
        );
        
        // Then: Response is successful
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        FeedResponse feed = response.getBody();
        assertThat(feed.getPosts()).hasSize(2);
        assertThat(feed.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(feed.getPageable().getPageSize()).isEqualTo(20);
        assertThat(feed.getTotalElements()).isEqualTo(2);
        
        // Then: Posts are enriched with metadata
        assertThat(feed.getPosts().get(0).getLikeCount()).isNotNull();
        assertThat(feed.getPosts().get(0).getCommentCount()).isNotNull();
        assertThat(feed.getPosts().get(0).getLikedByCurrentUser()).isNotNull();
    }

    @Test
    void testGetFeed_WithNoFollows_ReturnsEmptyFeed() {
        // Given: User doesn't follow anyone
        Long currentUserId = 1L;
        
        when(followServiceClient.getFollowingIds(eq(currentUserId), anyString()))
                .thenReturn(List.of());
        
        // When: Request feed
        ResponseEntity<FeedResponse> response = restTemplate.exchange(
                "/api/feed?page=0&size=20",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                FeedResponse.class
        );
        
        // Then: Response is successful with empty feed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        FeedResponse feed = response.getBody();
        assertThat(feed.getPosts()).isEmpty();
        assertThat(feed.getTotalElements()).isEqualTo(0);
    }

    @Test
    void testGetFeed_WithPagination_ReturnsCorrectPage() {
        // Given: User follows one user with multiple posts
        Long currentUserId = 1L;
        List<Long> followingIds = List.of(2L);
        
        when(followServiceClient.getFollowingIds(eq(currentUserId), anyString()))
                .thenReturn(followingIds);
        
        // Given: Multiple posts from followed user
        List<PostResponse> posts = List.of(
                PostResponse.builder().id(101L).userId(2L).content("Post 1")
                        .createdAt(LocalDateTime.now().minusHours(1))
                        .updatedAt(LocalDateTime.now().minusHours(1)).build(),
                PostResponse.builder().id(102L).userId(2L).content("Post 2")
                        .createdAt(LocalDateTime.now().minusHours(2))
                        .updatedAt(LocalDateTime.now().minusHours(2)).build(),
                PostResponse.builder().id(103L).userId(2L).content("Post 3")
                        .createdAt(LocalDateTime.now().minusHours(3))
                        .updatedAt(LocalDateTime.now().minusHours(3)).build()
        );
        
        when(postServiceClient.getPostsByUser(eq(2L), anyInt(), anyInt(), anyString()))
                .thenReturn(PostListResponse.builder()
                        .posts(posts)
                        .page(0)
                        .size(100)
                        .totalElements(3)
                        .totalPages(1)
                        .build());
        
        // Given: Metadata
        when(likeServiceClient.getBatchLikeCounts(anyList(), anyString()))
                .thenReturn(new HashMap<>());
        when(likeServiceClient.checkIfUserLiked(anyLong(), anyString()))
                .thenReturn(false);
        when(commentServiceClient.getCommentCount(anyLong(), anyString()))
                .thenReturn(0L);
        
        // When: Request first page with size 2
        ResponseEntity<FeedResponse> response = restTemplate.exchange(
                "/api/feed?page=0&size=2",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                FeedResponse.class
        );
        
        // Then: Response contains first 2 posts
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        FeedResponse feed = response.getBody();
        assertThat(feed.getPosts()).hasSize(2);
        assertThat(feed.getTotalElements()).isEqualTo(3);
        assertThat(feed.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testGetFeed_WithoutUserId_ReturnsBadRequest() {
        // When: Request feed without X-User-Id header
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/feed?page=0&size=20",
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class
        );
        
        // Then: Response is bad request (missing required header)
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
