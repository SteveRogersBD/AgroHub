package com.example.agrohub.data.repository

import com.example.agrohub.data.mapper.NotificationMapper
import com.example.agrohub.data.mapper.PagedDataMapper
import com.example.agrohub.data.remote.api.NotificationApiService
import com.example.agrohub.domain.model.Notification
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.repository.NotificationRepository
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Implementation of NotificationRepository that handles notification operations.
 * 
 * This repository manages notifications by communicating with the backend API,
 * including retrieving notifications, filtering unread notifications, and marking
 * notifications as read.
 *
 * @property notificationApiService The Retrofit service for notification endpoints
 */
class NotificationRepositoryImpl(
    private val notificationApiService: NotificationApiService
) : NotificationRepository {
    
    override suspend fun getNotifications(page: Int, size: Int): Result<PagedData<Notification>> {
        return try {
            val response = notificationApiService.getNotifications(page, size)
            
            // Map DTO to domain model
            val pagedData = PagedDataMapper.toDomain(response) { notificationDto ->
                NotificationMapper.toDomain(notificationDto)
            }
            
            Result.Success(pagedData)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun getUnreadNotifications(page: Int, size: Int): Result<PagedData<Notification>> {
        return try {
            val response = notificationApiService.getUnreadNotifications(page, size)
            
            // Map DTO to domain model
            val pagedData = PagedDataMapper.toDomain(response) { notificationDto ->
                NotificationMapper.toDomain(notificationDto)
            }
            
            Result.Success(pagedData)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun markAsRead(notificationId: Long): Result<Unit> {
        return try {
            notificationApiService.markAsRead(notificationId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun markAllAsRead(): Result<Unit> {
        return try {
            notificationApiService.markAllAsRead()
            Result.Success(Unit)
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
            404 -> AppException.NotFoundException("Notification not found")
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
