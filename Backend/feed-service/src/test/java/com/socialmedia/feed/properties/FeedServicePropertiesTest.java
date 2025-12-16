package com.socialmedia.feed.properties;

import com.socialmedia.feed.client.CommentServiceClient;
import com.socialmedia.feed.client.FollowServiceClient;
import com.socialmedia.feed.client.LikeServiceClient;
import com.socialmedia.feed.client.PostServiceClient;
import com.socialmedia.feed.client.UserServiceClient;
import com.socialmedia.feed.dto.EnrichedPostResponse;
import com.socialmedia.feed.dto.FeedResponse;
import com.socialmedia.feed.dto.PostListResponse;
import com.socialmedia.feed.dto.PostResponse;
import com.socialmedia.feed.service.FeedService;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Property-Based Tests for Feed Service
 * Feature: social-media-backend
 */
public class FeedServicePropertiesTest {

    private FeedService createFeedService() {
        FollowServiceClient followServiceClient = Mockito.mock(FollowServiceClient.class);
        PostServiceClient postServiceClient = Mockito.mock(PostServiceClient.class);
        LikeServiceClient likeServiceClient = Mockito.mock(LikeServiceClient.class);
        CommentServiceClient commentServiceClient = Mockito.mock(CommentServiceClient.class);
        UserServiceClient userServiceClient = Mockito.mock(UserServiceClient.class);
        
        return new FeedService(
            followServiceClient,
            postServiceClient,
            likeServiceClient,
            commentServiceClient,
            userServiceClient
        );
    }
    
    private FollowServiceClient mockFollowServiceClient;
    private PostServiceClient mockPostServiceClient;
    private LikeServiceClient mockLikeServiceClient;
    private CommentServiceClient mockCommentServiceClient;

    /**
     * Property 56: Feed contains posts from followed users
     * Validates: Requirements 17.1
     * 
     * For any user, their feed should contain only posts from users they follow,
     * ordered in reverse chronological order.
     */
    @Property(tries = 100)
    void feedContainsPostsFromFollowedUsers(
            @ForAll("userId") Long currentUserId,
            @ForAll("followedUsersList") List<Long> followedUserIds,
            @ForAll("postsMap") Map<Long, List<PostResponse>> userPosts) {
        
        Assume.that(!followedUserIds.isEmpty());
        Assume.that(userPosts.size() > 0);
        
        // Setup: Create mocks for this test
        mockFollowServiceClient = Mockito.mock(FollowServiceClient.class);
        mockPostServiceClient = Mockito.mock(PostServiceClient.class);
        mockLikeServiceClient = Mockito.mock(LikeServiceClient.class);
        mockCommentServiceClient = Mockito.mock(CommentServiceClient.class);
        UserServiceClient mockUserServiceClient = Mockito.mock(UserServiceClient.class);
        
        FeedService feedService = new FeedService(
            mockFollowServiceClient,
            mockPostServiceClient,
            mockLikeServiceClient,
            mockCommentServiceClient,
            mockUserServiceClient
        );
        
        // Setup: Mock follow service to return the followed users
        when(mockFollowServiceClient.getFollowingIds(eq(currentUserId), anyString()))
                .thenReturn(followedUserIds);
        
        // Setup: Mock post service to return posts for each followed user
        for (Long userId : followedUserIds) {
            List<PostResponse> posts = userPosts.getOrDefault(userId, List.of());
            PostListResponse postListResponse = PostListResponse.builder()
                    .posts(posts)
                    .page(0)
                    .size(100)
                    .totalElements(posts.size())
                    .totalPages(1)
                    .build();
            when(mockPostServiceClient.getPostsByUser(eq(userId), anyInt(), anyInt(), anyString()))
                    .thenReturn(postListResponse);
        }
        
        // Setup: Mock like and comment services
        when(mockLikeServiceClient.getBatchLikeCounts(anyList(), anyString()))
                .thenReturn(new HashMap<>());
        when(mockLikeServiceClient.checkIfUserLiked(anyLong(), anyString()))
                .thenReturn(false);
        when(mockCommentServiceClient.getCommentCount(anyLong(), anyString()))
                .thenReturn(0L);
        
        // When: Generate feed
        FeedResponse feed = feedService.generateFeed(currentUserId, 0, 20, "test-token");
        
        // Then: All posts in feed should be from followed users
        Set<Long> followedSet = new HashSet<>(followedUserIds);
        for (EnrichedPostResponse post : feed.getPosts()) {
            assertThat(followedSet).contains(post.getUserId());
        }
        
        // Then: Posts should be in reverse chronological order
        List<LocalDateTime> timestamps = feed.getPosts().stream()
                .map(EnrichedPostResponse::getCreatedAt)
                .collect(Collectors.toList());
        
        for (int i = 0; i < timestamps.size() - 1; i++) {
            assertThat(timestamps.get(i))
                    .isAfterOrEqualTo(timestamps.get(i + 1));
        }
    }

    /**
     * Property 58: Feed excludes soft-deleted posts
     * Validates: Requirements 17.4
     * 
     * For any user's feed, soft-deleted posts should not appear in the results.
     * Note: This property relies on the Post Service correctly filtering soft-deleted posts.
     */
    @Property(tries = 100)
    void feedExcludesSoftDeletedPosts(
            @ForAll("userId") Long currentUserId,
            @ForAll("followedUsersList") List<Long> followedUserIds,
            @ForAll("postsWithDeletedFlag") List<PostWithDeletedFlag> allPosts) {
        
        Assume.that(!followedUserIds.isEmpty());
        Assume.that(!allPosts.isEmpty());
        
        // Setup: Create mocks for this test
        mockFollowServiceClient = Mockito.mock(FollowServiceClient.class);
        mockPostServiceClient = Mockito.mock(PostServiceClient.class);
        mockLikeServiceClient = Mockito.mock(LikeServiceClient.class);
        mockCommentServiceClient = Mockito.mock(CommentServiceClient.class);
        UserServiceClient mockUserServiceClient = Mockito.mock(UserServiceClient.class);
        
        FeedService feedService = new FeedService(
            mockFollowServiceClient,
            mockPostServiceClient,
            mockLikeServiceClient,
            mockCommentServiceClient,
            mockUserServiceClient
        );
        
        // Setup: Mock follow service
        when(mockFollowServiceClient.getFollowingIds(eq(currentUserId), anyString()))
                .thenReturn(followedUserIds);
        
        // Setup: Mock post service to return only non-deleted posts (as it should)
        List<PostResponse> nonDeletedPosts = allPosts.stream()
                .filter(p -> !p.isDeleted())
                .map(p -> p.toPostResponse())
                .collect(Collectors.toList());
        
        for (Long userId : followedUserIds) {
            List<PostResponse> userSpecificPosts = nonDeletedPosts.stream()
                    .filter(p -> p.getUserId().equals(userId))
                    .collect(Collectors.toList());
            
            PostListResponse postListResponse = PostListResponse.builder()
                    .posts(userSpecificPosts)
                    .page(0)
                    .size(100)
                    .totalElements(userSpecificPosts.size())
                    .totalPages(1)
                    .build();
            when(mockPostServiceClient.getPostsByUser(eq(userId), anyInt(), anyInt(), anyString()))
                    .thenReturn(postListResponse);
        }
        
        // Setup: Mock like and comment services
        when(mockLikeServiceClient.getBatchLikeCounts(anyList(), anyString()))
                .thenReturn(new HashMap<>());
        when(mockLikeServiceClient.checkIfUserLiked(anyLong(), anyString()))
                .thenReturn(false);
        when(mockCommentServiceClient.getCommentCount(anyLong(), anyString()))
                .thenReturn(0L);
        
        // When: Generate feed
        FeedResponse feed = feedService.generateFeed(currentUserId, 0, 20, "test-token");
        
        // Then: Feed should not contain any deleted posts
        Set<Long> deletedPostIds = allPosts.stream()
                .filter(PostWithDeletedFlag::isDeleted)
                .map(p -> p.getId())
                .collect(Collectors.toSet());
        
        for (EnrichedPostResponse post : feed.getPosts()) {
            assertThat(deletedPostIds).doesNotContain(post.getId());
        }
    }

    /**
     * Property 59: Feed includes post metadata
     * Validates: Requirements 17.5
     * 
     * For any post in a feed, the response should include metadata 
     * such as like count and comment count.
     */
    @Property(tries = 100)
    void feedIncludesPostMetadata(
            @ForAll("userId") Long currentUserId,
            @ForAll("followedUsersList") List<Long> followedUserIds,
            @ForAll("postsMap") Map<Long, List<PostResponse>> userPosts,
            @ForAll("metadataMap") Map<Long, PostMetadata> metadata) {
        
        Assume.that(!followedUserIds.isEmpty());
        Assume.that(userPosts.size() > 0);
        
        // Setup: Create mocks for this test
        mockFollowServiceClient = Mockito.mock(FollowServiceClient.class);
        mockPostServiceClient = Mockito.mock(PostServiceClient.class);
        mockLikeServiceClient = Mockito.mock(LikeServiceClient.class);
        mockCommentServiceClient = Mockito.mock(CommentServiceClient.class);
        UserServiceClient mockUserServiceClient = Mockito.mock(UserServiceClient.class);
        
        FeedService feedService = new FeedService(
            mockFollowServiceClient,
            mockPostServiceClient,
            mockLikeServiceClient,
            mockCommentServiceClient,
            mockUserServiceClient
        );
        
        // Setup: Mock follow service
        when(mockFollowServiceClient.getFollowingIds(eq(currentUserId), anyString()))
                .thenReturn(followedUserIds);
        
        // Setup: Mock post service
        for (Long userId : followedUserIds) {
            List<PostResponse> posts = userPosts.getOrDefault(userId, List.of());
            PostListResponse postListResponse = PostListResponse.builder()
                    .posts(posts)
                    .page(0)
                    .size(100)
                    .totalElements(posts.size())
                    .totalPages(1)
                    .build();
            when(mockPostServiceClient.getPostsByUser(eq(userId), anyInt(), anyInt(), anyString()))
                    .thenReturn(postListResponse);
        }
        
        // Setup: Mock like service with specific metadata
        Map<Long, Long> likeCounts = new HashMap<>();
        for (Map.Entry<Long, PostMetadata> entry : metadata.entrySet()) {
            likeCounts.put(entry.getKey(), entry.getValue().getLikeCount());
            when(mockLikeServiceClient.checkIfUserLiked(eq(entry.getKey()), anyString()))
                    .thenReturn(entry.getValue().isLikedByUser());
        }
        when(mockLikeServiceClient.getBatchLikeCounts(anyList(), anyString()))
                .thenReturn(likeCounts);
        
        // Setup: Mock comment service with specific metadata
        for (Map.Entry<Long, PostMetadata> entry : metadata.entrySet()) {
            when(mockCommentServiceClient.getCommentCount(eq(entry.getKey()), anyString()))
                    .thenReturn(entry.getValue().getCommentCount());
        }
        
        // When: Generate feed
        FeedResponse feed = feedService.generateFeed(currentUserId, 0, 20, "test-token");
        
        // Then: All posts should have metadata fields populated
        for (EnrichedPostResponse post : feed.getPosts()) {
            assertThat(post.getLikeCount()).isNotNull();
            assertThat(post.getCommentCount()).isNotNull();
            assertThat(post.getLikedByCurrentUser()).isNotNull();
        }
    }

    // ========== Arbitraries (Generators) ==========

    @Provide
    Arbitrary<Long> userId() {
        return Arbitraries.longs().between(1L, 10000L);
    }

    @Provide
    Arbitrary<List<Long>> followedUsersList() {
        return Arbitraries.longs().between(1L, 10000L)
                .list().ofMinSize(1).ofMaxSize(10)
                .map(list -> list.stream().distinct().collect(Collectors.toList()));
    }

    @Provide
    Arbitrary<Map<Long, List<PostResponse>>> postsMap() {
        return Arbitraries.longs().between(1L, 10000L)
                .list().ofMinSize(1).ofMaxSize(5)
                .map(userIds -> userIds.stream().distinct().collect(Collectors.toList()))
                .flatMap(userIds -> {
                    List<Arbitrary<Map.Entry<Long, List<PostResponse>>>> entryArbitraries = userIds.stream()
                            .map(userId -> postResponseList().map(posts -> 
                                Map.entry(userId, posts.stream()
                                        .map(p -> PostResponse.builder()
                                                .id(p.getId())
                                                .userId(userId)
                                                .content(p.getContent())
                                                .mediaUrl(p.getMediaUrl())
                                                .createdAt(p.getCreatedAt())
                                                .updatedAt(p.getUpdatedAt())
                                                .build())
                                        .collect(Collectors.toList()))
                            ))
                            .collect(Collectors.toList());
                    
                    return Combinators.combine(entryArbitraries).as(entries -> {
                        Map<Long, List<PostResponse>> map = new HashMap<>();
                        for (Object entry : entries) {
                            @SuppressWarnings("unchecked")
                            Map.Entry<Long, List<PostResponse>> e = (Map.Entry<Long, List<PostResponse>>) entry;
                            map.put(e.getKey(), e.getValue());
                        }
                        return map;
                    });
                });
    }

    @Provide
    Arbitrary<List<PostResponse>> postResponseList() {
        return postResponse().list().ofMinSize(0).ofMaxSize(5);
    }

    @Provide
    Arbitrary<PostResponse> postResponse() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 100000L),
                Arbitraries.longs().between(1L, 10000L),
                Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(100),
                Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(50).injectNull(0.5),
                Arbitraries.integers().between(0, 365)
        ).as((id, userId, content, mediaUrl, daysAgo) -> 
                PostResponse.builder()
                        .id(id)
                        .userId(userId)
                        .content(content)
                        .mediaUrl(mediaUrl)
                        .createdAt(LocalDateTime.now().minusDays(daysAgo))
                        .updatedAt(LocalDateTime.now().minusDays(daysAgo))
                        .build()
        );
    }

    @Provide
    Arbitrary<List<PostWithDeletedFlag>> postsWithDeletedFlag() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 100000L),
                Arbitraries.longs().between(1L, 10000L),
                Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(100),
                Arbitraries.of(true, false),
                Arbitraries.integers().between(0, 365)
        ).as((id, userId, content, deleted, daysAgo) -> 
                new PostWithDeletedFlag(
                        id, userId, content, null,
                        LocalDateTime.now().minusDays(daysAgo),
                        LocalDateTime.now().minusDays(daysAgo),
                        deleted
                )
        ).list().ofMinSize(1).ofMaxSize(10);
    }

    @Provide
    Arbitrary<Map<Long, PostMetadata>> metadataMap() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 100000L),
                Arbitraries.longs().between(0L, 1000L),
                Arbitraries.longs().between(0L, 500L),
                Arbitraries.of(true, false)
        ).as((postId, likeCount, commentCount, liked) -> 
                Map.entry(postId, new PostMetadata(likeCount, commentCount, liked))
        ).list().ofMinSize(1).ofMaxSize(10)
        .map(entries -> {
            Map<Long, PostMetadata> map = new HashMap<>();
            for (Map.Entry<Long, PostMetadata> entry : entries) {
                map.put(entry.getKey(), entry.getValue());
            }
            return map;
        });
    }

    // ========== Helper Classes ==========

    private static class PostWithDeletedFlag {
        private final Long id;
        private final Long userId;
        private final String content;
        private final String mediaUrl;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;
        private final boolean deleted;

        public PostWithDeletedFlag(Long id, Long userId, String content, String mediaUrl,
                                  LocalDateTime createdAt, LocalDateTime updatedAt, boolean deleted) {
            this.id = id;
            this.userId = userId;
            this.content = content;
            this.mediaUrl = mediaUrl;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.deleted = deleted;
        }

        public Long getId() { return id; }
        public Long getUserId() { return userId; }
        public boolean isDeleted() { return deleted; }

        public PostResponse toPostResponse() {
            return PostResponse.builder()
                    .id(id)
                    .userId(userId)
                    .content(content)
                    .mediaUrl(mediaUrl)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
        }
    }

    private static class PostMetadata {
        private final Long likeCount;
        private final Long commentCount;
        private final boolean likedByUser;

        public PostMetadata(Long likeCount, Long commentCount, boolean likedByUser) {
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.likedByUser = likedByUser;
        }

        public Long getLikeCount() { return likeCount; }
        public Long getCommentCount() { return commentCount; }
        public boolean isLikedByUser() { return likedByUser; }
    }
}
