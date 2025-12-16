package com.example.agrohub.data.repository

import com.example.agrohub.data.remote.api.LikeApiService
import com.example.agrohub.data.remote.dto.BatchLikeCountRequestDto
import com.example.agrohub.domain.repository.LikeRepository
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Implementation of LikeRepository that handles like operations.
 * 
 * This repository manages liking/unliking posts, checking like status, and retrieving like counts.
 * All like/unlike operations are idempotent as per the backend API design.
 *
 * @property likeApiService The Retrofit service for like endpoints
 */
class LikeRepositoryImpl(
    private val likeApiService: LikeApiService
) : LikeRepository {
    
    /**
     * Likes a post. This operation is idempotent - calling it multiple times
     * for the same post has the same effect as calling it once.
     *
     * @param postId ID of the post to like
     * @return Result indicating success or failure
     */
    override suspend fun likePost(postId: Long): Result<Unit> {
        return try {
            likeApiService.likePost(postId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    /**
     * Unlikes a post. This operation is idempotent - calling it multiple times
     * for the same post has the same effect as calling it once.
     *
     * @param postId ID of the post to unlike
     * @return Result indicating success or failure
     */
    override suspend fun unlikePost(postId: Long): Result<Unit> {
        return try {
            likeApiService.unlikePost(postId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    /**
     * Checks if the current user has liked the specified post.
     *
     * @param postId ID of the post to check
     * @return Result containing true if liked, false otherwise, or an error
     */
    override suspend fun checkLikeStatus(postId: Long): Result<Boolean> {
        return try {
            val response = likeApiService.checkLikeStatus(postId)
            Result.Success(response.isLiked)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    /**
     * Retrieves the like count for a specific post.
     *
     * @param postId ID of the post
     * @return Result containing the like count on success, or an error
     */
    override suspend fun getLikeCount(postId: Long): Result<Int> {
        return try {
            val response = likeApiService.getLikeCount(postId)
            Result.Success(response.count)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    /**
     * Retrieves like counts for multiple posts in a single request.
     * The backend returns counts as a map with string keys, which we convert to Long keys.
     *
     * @param postIds List of post IDs to get counts for
     * @return Result containing a map of post ID to like count on success, or an error
     */
    override suspend fun getBatchLikeCounts(postIds: List<Long>): Result<Map<Long, Int>> {
        return try {
            val request = BatchLikeCountRequestDto(postIds)
            val response = likeApiService.getBatchLikeCounts(request)
            
            // Convert string keys to Long keys
            val countsMap = response.counts.mapKeys { (key, _) -> 
                key.toLongOrNull() ?: throw IllegalArgumentException("Invalid post ID in response: $key")
            }
            
            Result.Success(countsMap)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
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
            403 -> AppException.AuthorizationException("You don't have permission to perform this action")
            404 -> AppException.NotFoundException("Post not found")
            409 -> AppException.ValidationException("Like operation conflict")
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
