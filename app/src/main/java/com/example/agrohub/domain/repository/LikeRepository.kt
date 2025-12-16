package com.example.agrohub.domain.repository

import com.example.agrohub.domain.util.Result

/**
 * Repository interface for like operations.
 * Handles liking/unliking posts, checking like status, and retrieving like counts.
 */
interface LikeRepository {
    /**
     * Likes a post. This operation is idempotent.
     *
     * @param postId ID of the post to like
     * @return Result indicating success or failure
     */
    suspend fun likePost(postId: Long): Result<Unit>
    
    /**
     * Unlikes a post. This operation is idempotent.
     *
     * @param postId ID of the post to unlike
     * @return Result indicating success or failure
     */
    suspend fun unlikePost(postId: Long): Result<Unit>
    
    /**
     * Checks if the current user has liked the specified post.
     *
     * @param postId ID of the post to check
     * @return Result containing true if liked, false otherwise, or an error
     */
    suspend fun checkLikeStatus(postId: Long): Result<Boolean>
    
    /**
     * Retrieves the like count for a specific post.
     *
     * @param postId ID of the post
     * @return Result containing the like count on success, or an error
     */
    suspend fun getLikeCount(postId: Long): Result<Int>
    
    /**
     * Retrieves like counts for multiple posts in a single request.
     *
     * @param postIds List of post IDs to get counts for
     * @return Result containing a map of post ID to like count on success, or an error
     */
    suspend fun getBatchLikeCounts(postIds: List<Long>): Result<Map<Long, Int>>
}
