package com.example.agrohub.domain.model

/**
 * Domain model representing the actor (user who triggered) a notification.
 * This is a lightweight representation used in notification displays.
 *
 * @property id Unique identifier for the actor
 * @property username Actor's username
 * @property avatarUrl URL to actor's avatar image (null if not set)
 */
data class NotificationActor(
    val id: Long,
    val username: String,
    val avatarUrl: String?
)
