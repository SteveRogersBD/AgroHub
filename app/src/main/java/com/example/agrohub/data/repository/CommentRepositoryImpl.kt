package com.example.agrohub.data.repository

import com.example.agrohub.data.mapper.CommentMapper
import com.example.agrohub.data.mapper.PagedDataMapper
import com.example.agrohub.data.remote.api.CommentApiService
import com.example.agrohub.data.remote.dto.CreateCommentRequestDto
import com.example.agrohub.data.remote.dto.UpdateCommentRequestDto
import com.example.agrohub.domain.model.Comment
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.repository.CommentRepository
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Implementation of CommentRepository that handles comment operations.
 * 
 * This repository manages comment CRUD operations by communicating with the backend API.
 * It includes content validation, error mapping, and DTO to domain model conversion.
 *
 * @property commentApiService The Retrofit service for comment endpoints
 */
class CommentRepositoryImpl(
    private val commentApiService: CommentApiService
) : CommentRepository {
    
    override suspend fun createComment(postId: Long, content: String): Result<Comment> {
        return try {
            // Validate content before making API call
            if (content.isBlank()) {
                return Result.Error(AppException.ValidationException("Comment content cannot be empty"))
            }
            
            val request = CreateCommentRequestDto(
                postId = postId,
                content = content.trim()
            )
            
            val response = commentApiService.createComment(request)
            val comment = CommentMapper.toDomain(response)
            
            Result.Success(comment)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun updateComment(commentId: Long, content: String): Result<Comment> {
        return try {
            // Validate content before making API call
            if (content.isBlank()) {
                return Result.Error(AppException.ValidationException("Comment content cannot be empty"))
            }
            
            val request = UpdateCommentRequestDto(
                content = content.trim()
            )
            
            val response = commentApiService.updateComment(commentId, request)
            val comment = CommentMapper.toDomain(response)
            
            Result.Success(comment)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun deleteComment(commentId: Long): Result<Unit> {
        return try {
            commentApiService.deleteComment(commentId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun getPostComments(postId: Long, page: Int, size: Int): Result<PagedData<Comment>> {
        return try {
            val response = commentApiService.getPostComments(postId, page, size)
            val pagedData = PagedDataMapper.toCommentPagedData(response)
            
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
            404 -> AppException.NotFoundException("Comment not found")
            409 -> AppException.ValidationException("Comment conflict")
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
