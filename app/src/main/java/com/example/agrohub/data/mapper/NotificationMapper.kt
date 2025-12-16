package com.example.agrohub.data.mapper

import com.example.agrohub.data.remote.dto.NotificationDto
import com.example.agrohub.domain.model.Notification
import com.example.agrohub.domain.model.NotificationActor
import com.example.agrohub.domain.model.NotificationType

/**
 * Mapper for converting Notification DTOs to domain models.
 * Handles conversion from network layer representations to UI-friendly domain models,
 * including mapping string notification types to enum values.
 */
object NotificationMapper {
    
    /**
     * Converts a NotificationDto to a Notification domain model.
     * Maps the string type field to the NotificationType enum.
     *
     * @param dto The NotificationDto from the API
     * @return Notification domain model
     * @throws IllegalArgumentException if the notification type is not recognized
     * @throws Exception if required fields are missing or date parsing fails
     */
    fun toDomain(dto: NotificationDto): Notification {
        return Notification(
            id = dto.id,
            type = parseNotificationType(dto.type),
            actor = NotificationActor(
                id = dto.actorId,
                username = dto.actorUsername,
                avatarUrl = dto.actorAvatarUrl
            ),
            postId = dto.postId,
            message = dto.message,
            isRead = dto.isRead,
            createdAt = DateTimeUtils.parseDateTime(dto.createdAt)
        )
    }
    
    /**
     * Parses a string notification type to the NotificationType enum.
     * Handles case-insensitive matching.
     *
     * @param typeString The notification type as a string (e.g., "LIKE", "COMMENT", "FOLLOW")
     * @return NotificationType enum value
     * @throws IllegalArgumentException if the type string is not recognized
     */
    private fun parseNotificationType(typeString: String): NotificationType {
        return try {
            NotificationType.valueOf(typeString.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Unknown notification type: $typeString. Expected one of: LIKE, COMMENT, FOLLOW"
            )
        }
    }
    
    /**
     * Converts a list of NotificationDto to a list of Notification domain models.
     *
     * @param dtos List of NotificationDto from the API
     * @return List of Notification domain models
     */
    fun toDomainList(dtos: List<NotificationDto>): List<Notification> {
        return dtos.map { toDomain(it) }
    }
}
