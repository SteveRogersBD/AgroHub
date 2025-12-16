package com.example.agrohub.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrohub.domain.model.FeedPost
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.repository.FeedRepository
import com.example.agrohub.domain.repository.LikeRepository
import com.example.agrohub.domain.util.Result
import com.example.agrohub.domain.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for feed operations.
 * Manages feed state, pagination, and like/unlike operations for the UI layer.
 *
 * @property feedRepository Repository for feed operations
 * @property likeRepository Repository for like operations
 */
class FeedViewModel(
    private val feedRepository: FeedRepository,
    private val likeRepository: LikeRepository
) : ViewModel() {
    
    private val _feedState = MutableStateFlow<UiState<PagedData<FeedPost>>>(UiState.Idle)
    val feedState: StateFlow<UiState<PagedData<FeedPost>>> = _feedState.asStateFlow()
    
    private var currentPage = 0
    private val pageSize = 10
    
    /**
     * Loads the personalized feed for the current user.
     * Updates feedState through the following transitions:
     * Idle/Previous State → Loading → Success/Error
     *
     * @param refresh If true, resets pagination to page 0; if false, loads the next page
     */
    fun loadFeed(refresh: Boolean = false) {
        viewModelScope.launch {
            _feedState.value = UiState.Loading
            
            // Reset page to 0 if refreshing, otherwise use current page
            if (refresh) {
                currentPage = 0
            }
            
            when (val result = feedRepository.getPersonalizedFeed(currentPage, pageSize)) {
                is Result.Success -> {
                    _feedState.value = UiState.Success(result.data)
                    // Increment page for next load if not the last page
                    if (!result.data.isLastPage) {
                        currentPage++
                    }
                }
                is Result.Error -> _feedState.value = UiState.Error(result.exception.message)
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }
    
    /**
     * Likes a post and updates the feed state to reflect the change.
     * This operation is idempotent.
     *
     * @param postId ID of the post to like
     */
    fun likePost(postId: Long) {
        viewModelScope.launch {
            when (likeRepository.likePost(postId)) {
                is Result.Success -> {
                    // Update the feed state to reflect the like
                    updatePostLikeStatus(postId, isLiked = true)
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
     * Unlikes a post and updates the feed state to reflect the change.
     * This operation is idempotent.
     *
     * @param postId ID of the post to unlike
     */
    fun unlikePost(postId: Long) {
        viewModelScope.launch {
            when (likeRepository.unlikePost(postId)) {
                is Result.Success -> {
                    // Update the feed state to reflect the unlike
                    updatePostLikeStatus(postId, isLiked = false)
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
     * Toggles the like status of a post.
     * If the post is liked, it will be unliked, and vice versa.
     *
     * @param postId ID of the post to toggle like status
     */
    fun toggleLike(postId: Long) {
        val currentState = _feedState.value
        if (currentState is UiState.Success) {
            val post = currentState.data.items.find { it.id == postId }
            if (post != null) {
                if (post.isLikedByCurrentUser) {
                    unlikePost(postId)
                } else {
                    likePost(postId)
                }
            }
        }
    }
    
    /**
     * Updates the like status of a specific post in the current feed state.
     * This is an optimistic update that modifies the UI state immediately.
     *
     * @param postId ID of the post to update
     * @param isLiked New like status
     */
    private fun updatePostLikeStatus(postId: Long, isLiked: Boolean) {
        val currentState = _feedState.value
        if (currentState is UiState.Success) {
            val updatedPosts = currentState.data.items.map { post ->
                if (post.id == postId) {
                    post.copy(
                        isLikedByCurrentUser = isLiked,
                        likeCount = if (isLiked) post.likeCount + 1 else post.likeCount - 1
                    )
                } else {
                    post
                }
            }
            
            val updatedPagedData = currentState.data.copy(items = updatedPosts)
            _feedState.value = UiState.Success(updatedPagedData)
        }
    }
}
