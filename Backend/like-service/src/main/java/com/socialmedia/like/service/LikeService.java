package com.socialmedia.like.service;

import com.socialmedia.like.dto.*;
import com.socialmedia.like.entity.Like;
import com.socialmedia.like.mapper.LikeMapper;
import com.socialmedia.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final LikeRepository likeRepository;
    private final LikeMapper likeMapper;

    /**
     * Like a post (idempotent - creates if not exists)
     */
    @Transactional
    public LikeResponse likePost(Long postId, Long userId) {
        log.debug("User {} attempting to like post {}", userId, postId);

        // Check if like already exists (idempotent)
        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);
        
        if (existingLike.isPresent()) {
            log.debug("Like already exists for user {} on post {}", userId, postId);
            return likeMapper.toResponse(existingLike.get());
        }

        // Create new like
        Like like = likeMapper.toEntity(postId, userId);
        Like savedLike = likeRepository.save(like);
        
        log.info("User {} liked post {}", userId, postId);
        return likeMapper.toResponse(savedLike);
    }

    /**
     * Unlike a post (idempotent - deletes if exists)
     */
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        log.debug("User {} attempting to unlike post {}", userId, postId);

        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);
        
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            log.info("User {} unliked post {}", userId, postId);
        } else {
            log.debug("No like found for user {} on post {} - idempotent operation", userId, postId);
        }
    }

    /**
     * Get like count for a post
     */
    @Transactional(readOnly = true)
    public LikeCountResponse getLikeCount(Long postId) {
        log.debug("Getting like count for post {}", postId);
        
        Long count = likeRepository.countByPostId(postId);
        
        return LikeCountResponse.builder()
                .postId(postId)
                .count(count)
                .build();
    }

    /**
     * Check if user liked a post
     */
    @Transactional(readOnly = true)
    public LikeCheckResponse checkIfUserLikedPost(Long postId, Long userId) {
        log.debug("Checking if user {} liked post {}", userId, postId);
        
        boolean liked = likeRepository.existsByPostIdAndUserId(postId, userId);
        
        return LikeCheckResponse.builder()
                .postId(postId)
                .liked(liked)
                .build();
    }

    /**
     * Get like counts for multiple posts (batch operation)
     */
    @Transactional(readOnly = true)
    public BatchLikeCountResponse getBatchLikeCounts(List<Long> postIds) {
        log.debug("Getting like counts for {} posts", postIds.size());
        
        List<Object[]> results = likeRepository.countByPostIds(postIds);
        
        Map<Long, Long> likeCounts = new HashMap<>();
        
        // Initialize all post IDs with 0 count
        for (Long postId : postIds) {
            likeCounts.put(postId, 0L);
        }
        
        // Update with actual counts
        for (Object[] result : results) {
            Long postId = (Long) result[0];
            Long count = (Long) result[1];
            likeCounts.put(postId, count);
        }
        
        return BatchLikeCountResponse.builder()
                .likeCounts(likeCounts)
                .build();
    }
}
