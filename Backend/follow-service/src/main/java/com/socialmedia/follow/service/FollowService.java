package com.socialmedia.follow.service;

import com.socialmedia.follow.dto.*;
import com.socialmedia.follow.entity.Follow;
import com.socialmedia.follow.exception.BadRequestException;
import com.socialmedia.follow.mapper.FollowMapper;
import com.socialmedia.follow.repository.FollowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final FollowMapper followMapper;

    public FollowService(FollowRepository followRepository, FollowMapper followMapper) {
        this.followRepository = followRepository;
        this.followMapper = followMapper;
    }

    @Transactional
    public FollowResponse follow(Long followerId, Long followingId) {
        // Validate self-follow
        if (followerId.equals(followingId)) {
            throw new BadRequestException("Users cannot follow themselves");
        }

        // Check if already following (idempotent)
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            // Return existing relationship
            Follow existingFollow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                    .orElseThrow(() -> new BadRequestException("Follow relationship not found"));
            return followMapper.toResponse(existingFollow);
        }

        // Create new follow relationship
        Follow follow = followMapper.toEntity(followerId, followingId);
        Follow savedFollow = followRepository.save(follow);
        return followMapper.toResponse(savedFollow);
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        // Idempotent - no error if not following
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
        }
    }

    @Transactional(readOnly = true)
    public FollowerListResponse getFollowers(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Long> followerPage = followRepository.findFollowerIdsByFollowingId(userId, pageable);

        return FollowerListResponse.builder()
                .followerIds(followerPage.getContent())
                .page(followerPage.getNumber())
                .size(followerPage.getSize())
                .totalElements(followerPage.getTotalElements())
                .totalPages(followerPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public FollowingListResponse getFollowing(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Long> followingPage = followRepository.findFollowingIdsByFollowerId(userId, pageable);

        return FollowingListResponse.builder()
                .followingIds(followingPage.getContent())
                .page(followingPage.getNumber())
                .size(followingPage.getSize())
                .totalElements(followingPage.getTotalElements())
                .totalPages(followingPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public FollowStatsResponse getFollowStats(Long userId) {
        long followerCount = followRepository.countByFollowingId(userId);
        long followingCount = followRepository.countByFollowerId(userId);

        return FollowStatsResponse.builder()
                .userId(userId)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }

    @Transactional(readOnly = true)
    public FollowCheckResponse checkIfFollowing(Long followerId, Long followingId) {
        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);

        return FollowCheckResponse.builder()
                .followerId(followerId)
                .followingId(followingId)
                .isFollowing(isFollowing)
                .build();
    }
}
