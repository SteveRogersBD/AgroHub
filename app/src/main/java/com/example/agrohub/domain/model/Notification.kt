package com.example.agrohub.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing a notification in the system.
 *
 * @property id Unique identifier for the notification
 * @property type The type of notification (LIKE, COMMENT, FOLLOW)
 * @property actor Information about the user who triggered the notification
 * @property postId ID of the related post (null for FOLLOW notifications)
 * @property message Human-readable notification message
 * @property isRead Whether the notification has been read by the user
 * @property createdAt Timestamp when the notification was created
 */
data class Notification(
    val id: Long,
    val type: NotificationType,
    val actor: NotificationActor,
    val postId: Long?,
    val message: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)
