package com.example.agrohub.data.repository

import com.example.agrohub.data.cache.MemoryCache
import com.example.agrohub.data.mapper.PagedDataMapper
import com.example.agrohub.data.mapper.PostMapper
import com.example.agrohub.data.remote.api.FeedApiService
import com.example.agrohub.domain.model.FeedPost
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.repository.FeedRepository
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Implementation of FeedRepository that handles feed operations with caching support.
 * 
 * This repository manages the personalized feed by communicating with the backend API
 * and caching feed data for improved performance. The cache has a 2-minute TTL.
 *
 * @property feedApiService The Retrofit service for feed endpoints
 */
class FeedRepositoryImpl(
    private val feedApiService: FeedApiService
) : FeedRepository {
    
    // Feed cache with 2-minute TTL (as per requirement 22.4)
    private val feedCache = MemoryCache<String, List<FeedPost>>(
        maxSize = 10, // Cache up to 10 pages
        ttlMillis = 2 * 60 * 1000 // 2 minutes
    )
    
    // Key for the current feed cache
    private val FEED_CACHE_KEY = "current_feed"
    
    override suspend fun getPersonalizedFeed(page: Int, size: Int): Result<PagedData<FeedPost>> {
        return try {
            val response = feedApiService.getPersonalizedFeed(page, size)
            
            // Map DTO to domain model
            val pagedData = PagedDataMapper.toDomain(response) { feedPostDto ->
                PostMapper.feedPostToDomain(feedPostDto)
            }
            
            // Cache the feed items (only cache page 0 for simplicity)
            if (page == 0) {
                feedCache.put(FEED_CACHE_KEY, pagedData.items)
            }
            
            Result.Success(pagedData)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override fun getCachedFeed(): List<FeedPost> {
        return feedCache.get(FEED_CACHE_KEY) ?: emptyList()
    }
    
    /**
     * Maps exceptions to appropriate AppException types with user-friendly messages.
     *
     * @param e The exception to map
     * @return AppException with appropriate type and message
     */
    private fun mapException(e: Exception): AppException {
        return when (e) {
            is HttpException -> mapHttpException(e)
            is IOException -> mapNetworkException(e)
            else -> AppException.UnknownException(e.message ?: "An unknown error occurred")
        }
    }
    
    /**
     * Maps HTTP exceptions based on status code.
     *
     * @param e The HttpException to map
     * @return AppException with appropriate type and message
     */
    private fun mapHttpException(e: HttpException): AppException {
        return when (e.code()) {
            400 -> {
                // Try to parse error body for more specific message
                val errorMessage = try {
                    e.response()?.errorBody()?.string() ?: "Invalid request"
                } catch (ex: Exception) {
                    "Invalid request"
                }
                AppException.ValidationException(errorMessage)
            }
            401 -> AppException.AuthenticationException("Authentication required")
            403 -> AppException.AuthorizationException("You don't have permission to access the feed")
            404 -> AppException.NotFoundException("Feed not found")
            in 500..599 -> AppException.ServerException("Server error. Please try again later.")
            else -> AppException.UnknownException("An unexpected error occurred")
        }
    }
    
    /**
     * Maps network/IO exceptions to NetworkException.
     *
     * @param e The IOException to map
     * @return NetworkException with appropriate message
     */
    private fun mapNetworkException(e: IOException): AppException {
        return when (e) {
            is SocketTimeoutException -> 
                AppException.NetworkException("Request timed out. Please check your connection.")
            is UnknownHostException -> 
                AppException.NetworkException("Unable to reach server. Please check your internet connection.")
            else -> 
                AppException.NetworkException("Network error occurred. Please check your connection.")
        }
    }
}
