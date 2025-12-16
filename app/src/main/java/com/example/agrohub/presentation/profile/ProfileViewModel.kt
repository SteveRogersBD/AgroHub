package com.example.agrohub.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrohub.domain.model.FollowStats
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.model.Post
import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.repository.FollowRepository
import com.example.agrohub.domain.repository.UserRepository
import com.example.agrohub.domain.util.Result
import com.example.agrohub.domain.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for profile operations.
 * Manages profile state, follow stats, user posts, and follow/unfollow operations for the UI layer.
 *
 * @property userRepository Repository for user profile operations
 * @property followRepository Repository for follow operations
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository
) : ViewModel() {
    
    private val _profileState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val profileState: StateFlow<UiState<User>> = _profileState.asStateFlow()
    
    private val _followStatsState = MutableStateFlow<UiState<FollowStats>>(UiState.Idle)
    val followStatsState: StateFlow<UiState<FollowStats>> = _followStatsState.asStateFlow()
    
    private val _userPostsState = MutableStateFlow<UiState<PagedData<Post>>>(UiState.Idle)
    val userPostsState: StateFlow<UiState<PagedData<Post>>> = _userPostsState.asStateFlow()
    
    /**
     * Loads a user profile by user ID.
     * Updates profileState through the following transitions:
     * Idle/Previous State → Loading → Success/Error
     *
     * @param userId ID of the user to load
     */
    fun loadProfile(userId: Long) {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            when (val result = userRepository.getUserById(userId)) {
                is Result.Success -> _profileState.value = UiState.Success(result.data)
                is Result.Error -> _profileState.value = UiState.Error(result.exception.message)
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }
    
    /**
     * Loads follow statistics for a user.
     * Updates followStatsState through the following transitions:
     * Idle/Previous State → Loading → Success/Error
     *
     * @param userId ID of the user to load stats for
     */
    fun loadFollowStats(userId: Long) {
        viewModelScope.launch {
            _followStatsState.value = UiState.Loading
            when (val result = followRepository.getFollowStats(userId)) {
                is Result.Success -> _followStatsState.value = UiState.Success(result.data)
                is Result.Error -> _followStatsState.value = UiState.Error(result.exception.message)
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }
    
    /**
     * Follows a user and updates the follow stats to reflect the change.
     * This operation is idempotent.
     *
     * @param userId ID of the user to follow
     */
    fun followUser(userId: Long) {
        viewModelScope.launch {
            when (followRepository.followUser(userId)) {
                is Result.Success -> {
                    // Update the follow stats to reflect the follow
                    updateFollowStats(followersIncrement = 1)
                }
                is Result.Error -> {
                    // Optionally handle error - for now we silently fail
                    // Could emit a separate error state if needed
                }
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }
    
    /**
     * Unfollows a user and updates the follow stats to reflect the change.
     * This operation is idempotent.
     *
     * @param userId ID of the user to unfollow
     */
    fun unfollowUser(userId: Long) {
        viewModelScope.launch {
            when (followRepository.unfollowUser(userId)) {
                is Result.Success -> {
                    // Update the follow stats to reflect the unfollow
                    updateFollowStats(followersIncrement = -1)
                }
                is Result.Error -> {
                    // Optionally handle error - for now we silently fail
                    // Could emit a separate error state if needed
                }
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }
    
    /**
     * Updates the follow stats in the current state.
     * This is an optimistic update that modifies the UI state immediately.
     *
     * @param followersIncrement Amount to increment followers count (can be negative)
     */
    private fun updateFollowStats(followersIncrement: Int) {
        val currentState = _followStatsState.value
        if (currentState is UiState.Success) {
            val updatedStats = currentState.data.copy(
                followersCount = currentState.data.followersCount + followersIncrement
            )
            _followStatsState.value = UiState.Success(updatedStats)
        }
    }
}
