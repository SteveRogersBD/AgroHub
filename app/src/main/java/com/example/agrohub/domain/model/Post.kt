package com.example.agrohub.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing a basic post in the system.
 * This is used for post creation and individual post views.
 *
 * @property id Unique identifier for the post
 * @property userId ID of the user who created the post
 * @property content The text content of the post
 * @property mediaUrl URL to attached media (image/video), null if no media
 * @property createdAt Timestamp when the post was created
 * @property updatedAt Timestamp when the post was last updated (null if never updated)
 */
data class Post(
    val id: Long,
    val userId: Long,
    val content: String,
    val mediaUrl: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)
