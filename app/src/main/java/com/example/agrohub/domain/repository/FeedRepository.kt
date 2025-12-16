package com.example.agrohub.domain.repository

import com.example.agrohub.domain.model.FeedPost
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.util.Result

/**
 * Repository interface for feed operations.
 * Handles retrieving the personalized feed and managing feed cache.
 */
interface FeedRepository {
    /**
     * Retrieves the personalized feed for the current user.
     * The feed includes posts from followed users with enriched metadata.
     *
     * @param page Page number (0-indexed)
     * @param size Number of items per page
     * @return Result containing PagedData of FeedPosts on success, or an error
     */
    suspend fun getPersonalizedFeed(page: Int, size: Int): Result<PagedData<FeedPost>>
    
    /**
     * Retrieves the cached feed without making a network request.
     * Returns an empty list if no cached data is available.
     *
     * @return List of cached FeedPosts
     */
    fun getCachedFeed(): List<FeedPost>
}
