package com.example.agrohub.domain.repository

import com.example.agrohub.domain.model.Notification
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.util.Result

/**
 * Repository interface for notification operations.
 * Handles retrieving notifications and marking them as read.
 */
interface NotificationRepository {
    /**
     * Retrieves a paginated list of all notifications for the current user.
     *
     * @param page Page number (0-indexed)
     * @param size Number of items per page
     * @return Result containing PagedData of Notifications on success, or an error
     */
    suspend fun getNotifications(page: Int, size: Int): Result<PagedData<Notification>>
    
    /**
     * Retrieves a paginated list of unread notifications for the current user.
     *
     * @param page Page number (0-indexed)
     * @param size Number of items per page
     * @return Result containing PagedData of unread Notifications on success, or an error
     */
    suspend fun getUnreadNotifications(page: Int, size: Int): Result<PagedData<Notification>>
    
    /**
     * Marks a specific notification as read.
     *
     * @param notificationId ID of the notification to mark as read
     * @return Result indicating success or failure
     */
    suspend fun markAsRead(notificationId: Long): Result<Unit>
    
    /**
     * Marks all notifications as read for the current user.
     *
     * @return Result indicating success or failure
     */
    suspend fun markAllAsRead(): Result<Unit>
}
