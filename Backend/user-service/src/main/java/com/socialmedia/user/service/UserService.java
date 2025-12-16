package com.socialmedia.user.service;

import com.socialmedia.user.dto.UserProfileRequest;
import com.socialmedia.user.dto.UserProfileResponse;
import com.socialmedia.user.dto.UserSearchResponse;
import com.socialmedia.user.entity.UserProfile;
import com.socialmedia.user.exception.AuthorizationException;
import com.socialmedia.user.exception.ResourceNotFoundException;
import com.socialmedia.user.mapper.UserMapper;
import com.socialmedia.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserProfileResponse createProfile(Long userId, UserProfileRequest request) {
        UserProfile profile = userMapper.toEntity(request, userId);
        UserProfile savedProfile = userProfileRepository.save(profile);
        return userMapper.toResponse(savedProfile);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long profileId, Long currentUserId, UserProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + profileId));

        // Authorization check: users can only update their own profile
        if (!profile.getUserId().equals(currentUserId)) {
            throw new AuthorizationException("You are not authorized to update this profile");
        }

        userMapper.updateEntity(profile, request);
        UserProfile updatedProfile = userProfileRepository.save(profile);
        return userMapper.toResponse(updatedProfile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileById(Long profileId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + profileId));
        return userMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUserId(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user id: " + userId));
        return userMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Long currentUserId) {
        return getProfileByUserId(currentUserId);
    }

    @Transactional(readOnly = true)
    public UserSearchResponse searchByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<UserProfile> profilePage = userProfileRepository.searchByName(name, pageable);

        List<UserProfileResponse> users = profilePage.getContent().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return UserSearchResponse.builder()
                .users(users)
                .currentPage(profilePage.getNumber())
                .totalPages(profilePage.getTotalPages())
                .totalElements(profilePage.getTotalElements())
                .pageSize(profilePage.getSize())
                .build();
    }
}
