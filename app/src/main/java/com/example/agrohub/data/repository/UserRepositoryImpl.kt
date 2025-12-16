package com.example.agrohub.data.repository

import com.example.agrohub.data.cache.MemoryCache
import com.example.agrohub.data.mapper.PagedDataMapper
import com.example.agrohub.data.mapper.UserMapper
import com.example.agrohub.data.remote.api.UserApiService
import com.example.agrohub.data.remote.dto.CreateProfileRequestDto
import com.example.agrohub.data.remote.dto.UpdateProfileRequestDto
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.repository.UserRepository
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Implementation of UserRepository that handles user profile operations.
 * 
 * This repository manages user profile CRUD operations, user search, and caching
 * by communicating with the backend API and managing an in-memory cache.
 *
 * @property userApiService The Retrofit service for user endpoints
 * @property userCache In-memory cache for user profiles (5 minute TTL, max 100 entries)
 */
class UserRepositoryImpl(
    private val userApiService: UserApiService,
    private val userCache: MemoryCache<Long, User> = MemoryCache(maxSize = 100, ttlMillis = 5 * 60 * 1000)
) : UserRepository {
    
    override suspend fun createProfile(
        name: String?,
        bio: String?,
        avatarUrl: String?,
        location: String?,
        website: String?
    ): Result<User> {
        return try {
            val request = CreateProfileRequestDto(
                name = name,
                bio = bio,
                avatarUrl = avatarUrl,
                location = location,
                website = website
            )
            
            val response = userApiService.createProfile(request)
            val user = UserMapper.toDomain(response)
            
            // Cache the created profile
            userCache.put(user.id, user)
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun updateProfile(
        userId: Long,
        name: String?,
        bio: String?,
        avatarUrl: String?,
        location: String?,
        website: String?
    ): Result<User> {
        return try {
            val request = UpdateProfileRequestDto(
                name = name,
                bio = bio,
                avatarUrl = avatarUrl,
                location = location,
                website = website
            )
            
            val response = userApiService.updateProfile(userId, request)
            val user = UserMapper.toDomain(response)
            
            // Invalidate cache for this user and update with new data
            userCache.invalidate(userId)
            userCache.put(userId, user)
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = userApiService.getCurrentUser()
            val user = UserMapper.toDomain(response)
            
            // Cache the current user
            userCache.put(user.id, user)
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun getUserById(userId: Long): Result<User> {
        return try {
            // Check cache first
            val cachedUser = userCache.get(userId)
            if (cachedUser != null) {
                return Result.Success(cachedUser)
            }
            
            // Cache miss, fetch from API
            val response = userApiService.getUserById(userId)
            val user = UserMapper.toDomain(response)
            
            // Cache the fetched user
            userCache.put(userId, user)
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun getUserByUsername(username: String): Result<User> {
        return try {
            val response = userApiService.getUserByUsername(username)
            val user = UserMapper.toDomain(response)
            
            // Cache the fetched user
            userCache.put(user.id, user)
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun searchUsers(query: String, page: Int, size: Int): Result<PagedData<User>> {
        return try {
            val response = userApiService.searchUsers(query, page, size)
            val pagedData = PagedDataMapper.toUserPagedData(response)
            
            // Cache all users from search results
            pagedData.items.forEach { user ->
                userCache.put(user.id, user)
            }
            
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
            409 -> AppException.ValidationException("User already exists")
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
