package com.example.agrohub.data.remote.api

import com.example.agrohub.data.remote.dto.NotificationDto
import com.example.agrohub.data.remote.dto.PagedResponseDto
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service for notification endpoints.
 * 
 * Provides methods for notification management.
 */
interface NotificationApiService {
    
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedResponseDto<NotificationDto>
    
    @GET("notifications/unread")
    suspend fun getUnreadNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedResponseDto<NotificationDto>
    
    @PUT("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") notificationId: Long)
    
    @PUT("notifications/read-all")
    suspend fun markAllAsRead()
}
