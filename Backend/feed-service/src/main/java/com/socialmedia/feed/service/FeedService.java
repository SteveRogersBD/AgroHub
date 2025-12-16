package com.socialmedia.feed.service;

import com.socialmedia.feed.client.CommentServiceClient;
import com.socialmedia.feed.client.FollowServiceClient;
import com.socialmedia.feed.client.LikeServiceClient;
import com.socialmedia.feed.client.PostServiceClient;
import com.socialmedia.feed.client.UserServiceClient;
import com.socialmedia.feed.dto.EnrichedPostResponse;
import com.socialmedia.feed.dto.FeedResponse;
import com.socialmedia.feed.dto.PostResponse;
import com.socialmedia.feed.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final FollowServiceClient followServiceClient;
    private final PostServiceClient postServiceClient;
    private final LikeServiceClient likeServiceClient;
    private final CommentServiceClient commentServiceClient;
    private final UserServiceClient userServiceClient;

    public FeedResponse generateFeed(Long userId, int page, int size, String token) {
        log.debug("Generating feed for user {} - page: {}, size: {}", userId, page, size);

        // Step 1: Get list of users that the current user follows
        List<Long> followingIds = followServiceClient.getFollowingIds(userId, token);
        log.debug("User {} follows {} users", userId, followingIds.size());

        if (followingIds.isEmpty()) {
            // Return empty feed if user doesn't follow anyone
            return FeedResponse.builder()
                    .posts(List.of())
                    .pageable(FeedResponse.PageableInfo.builder()
                            .pageNumber(page)
                            .pageSize(size)
                            .build())
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build();
        }

        // Step 2: Fetch posts from all followed users
        List<PostResponse> allPosts = new ArrayList<>();
        for (Long followedUserId : followingIds) {
            var postList = postServiceClient.getPostsByUser(followedUserId, 0, 100, token);
            if (postList != null && postList.getPosts() != null) {
                allPosts.addAll(postList.getPosts());
            }
        }

        log.debug("Fetched {} total posts from followed users", allPosts.size());

        // Step 3: Sort posts in reverse chronological order
        allPosts.sort(Comparator.comparing(PostResponse::getCreatedAt).reversed());

        // Step 4: Apply pagination
        int start = page * size;
        int end = Math.min(start + size, allPosts.size());
        
        if (start >= allPosts.size()) {
            int totalPages = (int) Math.ceil((double) allPosts.size() / size);
            return FeedResponse.builder()
                    .posts(List.of())
                    .pageable(FeedResponse.PageableInfo.builder()
                            .pageNumber(page)
                            .pageSize(size)
                            .build())
                    .totalElements(allPosts.size())
                    .totalPages(totalPages)
                    .last(page >= totalPages - 1)
                    .build();
        }

        List<PostResponse> paginatedPosts = allPosts.subList(start, end);

        // Step 5: Enrich posts with metadata
        List<EnrichedPostResponse> enrichedPosts = enrichPostsWithMetadata(paginatedPosts, token);

        int totalPages = (int) Math.ceil((double) allPosts.size() / size);
        return FeedResponse.builder()
                .posts(enrichedPosts)
                .pageable(FeedResponse.PageableInfo.builder()
                        .pageNumber(page)
                        .pageSize(size)
                        .build())
                .totalElements(allPosts.size())
                .totalPages(totalPages)
                .last(page >= totalPages - 1)
                .build();
    }

    private List<EnrichedPostResponse> enrichPostsWithMetadata(List<PostResponse> posts, String token) {
        if (posts.isEmpty()) {
            return List.of();
        }

        // Get post IDs
        List<Long> postIds = posts.stream()
                .map(PostResponse::getId)
                .collect(Collectors.toList());

        // Batch fetch like counts
        Map<Long, Long> likeCounts = likeServiceClient.getBatchLikeCounts(postIds, token);

        // Get unique user IDs and fetch user profiles
        List<Long> userIds = posts.stream()
                .map(PostResponse::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, UserProfileResponse> userProfiles = userIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> userServiceClient.getUserProfile(userId, token)
                ));

        // Enrich each post
        return posts.stream()
                .map(post -> {
                    Long likeCount = likeCounts.getOrDefault(post.getId(), 0L);
                    Boolean likedByCurrentUser = likeServiceClient.checkIfUserLiked(post.getId(), token);
                    Long commentCount = commentServiceClient.getCommentCount(post.getId(), token);
                    
                    // Get user profile information
                    UserProfileResponse userProfile = userProfiles.get(post.getUserId());
                    String username = userProfile != null ? userProfile.getName() : "Unknown";
                    String userAvatarUrl = userProfile != null ? userProfile.getAvatarUrl() : null;

                    return EnrichedPostResponse.builder()
                            .id(post.getId())
                            .userId(post.getUserId())
                            .username(username)
                            .userAvatarUrl(userAvatarUrl)
                            .content(post.getContent())
                            .mediaUrl(post.getMediaUrl())
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .likeCount(likeCount)
                            .commentCount(commentCount)
                            .likedByCurrentUser(likedByCurrentUser)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
