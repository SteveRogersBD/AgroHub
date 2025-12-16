package com.example.agrohub.domain.repository

import com.example.agrohub.domain.model.Comment
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.util.Result

/**
 * Repository interface for comment operations.
 * Handles comment creation, updates, deletion, and retrieval.
 */
interface CommentRepository {
    /**
     * Creates a new comment on a post.
     *
     * @param postId ID of the post to comment on
     * @param content The text content of the comment
     * @return Result containing the created Comment on success, or an error
     */
    suspend fun createComment(postId: Long, content: String): Result<Comment>
    
    /**
     * Updates an existing comment.
     *
     * @param commentId ID of the comment to update
     * @param content The updated text content
     * @return Result containing the updated Comment on success, or an error
     */
    suspend fun updateComment(commentId: Long, content: String): Result<Comment>
    
    /**
     * Deletes a comment.
     *
     * @param commentId ID of the comment to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteComment(commentId: Long): Result<Unit>
    
    /**
     * Retrieves a paginated list of comments for a specific post.
     *
     * @param postId ID of the post
     * @param page Page number (0-indexed)
     * @param size Number of items per page
     * @return Result containing PagedData of Comments on success, or an error
     */
    suspend fun getPostComments(postId: Long, page: Int, size: Int): Result<PagedData<Comment>>
}
