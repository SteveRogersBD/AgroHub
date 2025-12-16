package com.example.agrohub.domain.model

/**
 * Enum representing the different types of notifications in the system.
 */
enum class NotificationType {
    /**
     * Notification for when someone likes a post
     */
    LIKE,
    
    /**
     * Notification for when someone comments on a post
     */
    COMMENT,
    
    /**
     * Notification for when someone follows the user
     */
    FOLLOW
}
