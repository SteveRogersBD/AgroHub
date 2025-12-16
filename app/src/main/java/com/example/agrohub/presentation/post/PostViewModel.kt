package com.example.agrohub.presentation.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrohub.domain.model.Comment
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.model.Post
import com.example.agrohub.domain.repository.CommentRepository
import com.example.agrohub.domain.repository.PostRepository
import com.example.agrohub.domain.util.Result
import com.example.agrohub.domain.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for post operations.
 * Manages post creation, comments state, and comment operations for the UI layer.
 *
 * @property postRepository Repository for post operations
 * @property commentRepository Repository for comment operations
 */
class PostViewModel(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {
    
    private val _createPostState = MutableStateFlow<UiState<Post>>(UiState.Idle)
    val createPostState: StateFlow<UiState<Post>> = _createPostState.asStateFlow()
    
    private val _commentsState = MutableStateFlow<UiState<PagedData<Comment>>>(UiState.Idle)
    val commentsState: StateFlow<UiState<PagedData<Comment>>> = _commentsState.asStateFlow()
    
    private var currentPostId: Long? = null
    private var currentPage = 0
    private val pageSize = 10
    
    /**
     * Creates a new post with the given content and optional media URL.
     * Updates createPostState through the following transitions:
     * Idle/Previous State → Loading → Success/Error
     *
     * @param content The text content of the post
     * @param mediaUrl URL to attached media (optional)
     */
    fun createPost(content: String, mediaUrl: String?) {
        viewModelScope.launch {
            _createPostState.value = UiState.Loading
            when (val result = postRepository.createPost(content, mediaUrl)) {
                is Result.Success -> _createPostState.value = UiState.Success(result.data)
                is Result.Error -> _createPostState.value = UiState.Error(result.exception.message)
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }
    
    /**
     * Loads comments for a specific post with pagination support.
     * Updates commentsState through the following transitions:
     * Idle/Previous State → Loading → Success/Error
     *
     * @param postId ID of the post to load comments for
     * @param page Page number (0-indexed), defaults to 0
     */
    fun loadComments(postId: Long, page: Int = 0) {
        viewModelScope.launch {
            _commentsState.value = UiState.Loading
            
            // Track the current post and page for pagination
            currentPostId = postId
            currentPage = page
            
            when (val result = commentRepository.getPostComments(postId, page, pageSize)) {
                is Result.Success -> {
                    _commentsState.value = UiState.Success(result.data)
                    // Increment page for next load if not the last page
                    if (!result.data.isLastPage) {
                        currentPage++
                    }
                }
                is Result.Error -> _commentsState.value = UiState.Error(result.exception.message)
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }
    
    /**
     * Adds a new comment to a post and refreshes the comments list.
     * This operation will reload comments from page 0 to show the new comment.
     *
     * @param postId ID of the post to comment on
     * @param content The text content of the comment
     */
    fun addComment(postId: Long, content: String) {
        viewModelScope.launch {
            when (commentRepository.createComment(postId, content)) {
                is Result.Success -> {
                    // Refresh comments from the beginning to show the new comment
                    loadComments(postId, page = 0)
                }
                is Result.Error -> {
                    // Optionally handle error - for now we silently fail
                    // Could emit a separate error state if needed
                }
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }
}
