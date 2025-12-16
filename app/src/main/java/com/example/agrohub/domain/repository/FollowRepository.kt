package com.example.agrohub.domain.repository

import com.example.agrohub.domain.model.FollowStats
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.util.Result

/**
 * Repository interface for follow/unfollow operations.
 * Handles following users, checking follow status, and retrieving follow statistics.
 */
interface FollowRepository {
    /**
     * Follows a user. This operation is idempotent.
     *
     * @param userId ID of the user to follow
     * @return Result indicating success or failure
     */
    suspend fun followUser(userId: Long): Result<Unit>
    
    /**
     * Unfollows a user. This operation is idempotent.
     *
     * @param userId ID of the user to unfollow
     * @return Result indicating success or failure
     */
    suspend fun unfollowUser(userId: Long): Result<Unit>
    
    /**
     * Checks if the current user is following the specified user.
     *
     * @param userId ID of the user to check
     * @return Result containing true if following, false otherwise, or an error
     */
    suspend fun checkFollowStatus(userId: Long): Result<Boolean>
    
    /**
     * Retrieves follow statistics for a user.
     *
     * @param userId ID of the user
     * @return Result containing FollowStats on success, or an error
     */
    suspend fun getFollowStats(userId: Long): Result<FollowStats>
    
    /**
     * Retrieves a paginated list of users following the specified user.
     *
     * @param userId ID of the user
     * @param page Page number (0-indexed)
     * @param size Number of items per page
     * @return Result containing PagedData of Users on success, or an error
     */
    suspend fun getFollowers(userId: Long, page: Int, size: Int): Result<PagedData<User>>
    
    /**
     * Retrieves a paginated list of users that the specified user is following.
     *
     * @param userId ID of the user
     * @param page Page number (0-indexed)
     * @param size Number of items per page
     * @return Result containing PagedData of Users on success, or an error
     */
    suspend fun getFollowing(userId: Long, page: Int, size: Int): Result<PagedData<User>>
}
