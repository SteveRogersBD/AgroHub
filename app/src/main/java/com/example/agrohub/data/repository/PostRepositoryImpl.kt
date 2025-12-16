package com.example.agrohub.data.repository

import com.example.agrohub.data.mapper.PagedDataMapper
import com.example.agrohub.data.mapper.PostMapper
import com.example.agrohub.data.remote.api.PostApiService
import com.example.agrohub.data.remote.dto.CreatePostRequestDto
import com.example.agrohub.data.remote.dto.UpdatePostRequestDto
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.model.Post
import com.example.agrohub.domain.repository.PostRepository
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Implementation of PostRepository that handles post operations.
 * 
 * This repository manages post CRUD operations by communicating with the backend API.
 * It includes content validation, error mapping, and DTO to domain model conversion.
 *
 * @property postApiService The Retrofit service for post endpoints
 */
class PostRepositoryImpl(
    private val postApiService: PostApiService
) : PostRepository {
    
    override suspend fun createPost(content: String, mediaUrl: String?): Result<Post> {
        return try {
            // Validate content before making API call
            if (content.isBlank()) {
                return Result.Error(AppException.ValidationException("Post content cannot be empty"))
            }
            
            val request = CreatePostRequestDto(
                content = content.trim(),
                mediaUrl = mediaUrl
            )
            
            val response = postApiService.createPost(request)
            val post = PostMapper.toDomain(response)
            
            Result.Success(post)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun updatePost(postId: Long, content: String, mediaUrl: String?): Result<Post> {
        return try {
            // Validate content before making API call
            if (content.isBlank()) {
                return Result.Error(AppException.ValidationException("Post content cannot be empty"))
            }
            
            val request = UpdatePostRequestDto(
                content = content.trim(),
                mediaUrl = mediaUrl
            )
            
            val response = postApiService.updatePost(postId, request)
            val post = PostMapper.toDomain(response)
            
            Result.Success(post)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun deletePost(postId: Long): Result<Unit> {
        return try {
            postApiService.deletePost(postId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun getPostById(postId: Long): Result<Post> {
        return try {
            val response = postApiService.getPostById(postId)
            val post = PostMapper.toDomain(response)
            
            Result.Success(post)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun getUserPosts(userId: Long, page: Int, size: Int): Result<PagedData<Post>> {
        return try {
            val response = postApiService.getUserPosts(userId, page, size)
            val pagedData = PagedDataMapper.toPostPagedData(response)
            
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
            403 -> AppException.AuthorizationException("You don't have permission to perform this action")
            404 -> AppException.NotFoundException("Post not found")
            409 -> AppException.ValidationException("Post conflict")
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
