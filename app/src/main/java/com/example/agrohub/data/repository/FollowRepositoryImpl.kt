package com.example.agrohub.data.repository

import com.example.agrohub.data.mapper.PagedDataMapper
import com.example.agrohub.data.mapper.UserMapper
import com.example.agrohub.data.remote.api.FollowApiService
import com.example.agrohub.domain.model.FollowStats
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.repository.FollowRepository
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Implementation of FollowRepository that handles follow/unfollow operations.
 * 
 * This repository manages user follow relationships, follow status checking,
 * and retrieval of follow statistics by communicating with the backend API.
 *
 * @property followApiService The Retrofit service for follow endpoints
 */
class FollowRepositoryImpl(
    private val followApiService: FollowApiService
) : FollowRepository {
    
    override suspend fun followUser(userId: Long): Result<Unit> {
        return try {
            followApiService.followUser(userId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun unfollowUser(userId: Long): Result<Unit> {
        return try {
            followApiService.unfollowUser(userId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun checkFollowStatus(userId: Long): Result<Boolean> {
        return try {
            val response = followApiService.checkFollowStatus(userId)
            Result.Success(response.isFollowing)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun getFollowStats(userId: Long): Result<FollowStats> {
        return try {
            val response = followApiService.getFollowStats(userId)
            val stats = FollowStats(
                followersCount = response.followersCount,
                followingCount = response.followingCount
            )
            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun getFollowers(userId: Long, page: Int, size: Int): Result<PagedData<User>> {
        return try {
            val response = followApiService.getFollowers(userId, page, size)
            val pagedData = PagedDataMapper.toUserPagedData(response)
            Result.Success(pagedData)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun getFollowing(userId: Long, page: Int, size: Int): Result<PagedData<User>> {
        return try {
            val response = followApiService.getFollowing(userId, page, size)
            val pagedData = PagedDataMapper.toUserPagedData(response)
            Result.Success(pagedData)
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
            403 -> AppException.AuthorizationException("Access denied")
            404 -> AppException.NotFoundException("User not found")
            409 -> AppException.ValidationException("Follow relationship already exists or doesn't exist")
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
