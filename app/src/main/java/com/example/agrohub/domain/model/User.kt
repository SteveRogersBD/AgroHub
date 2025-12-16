package com.example.agrohub.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing a user in the system.
 * This is the UI-friendly representation used throughout the application.
 *
 * @property id Unique identifier for the user
 * @property email User's email address
 * @property username User's unique username
 * @property name User's display name (defaults to empty string if not provided)
 * @property bio User's biography (defaults to empty string if not provided)
 * @property avatarUrl URL to user's avatar image (null if not set)
 * @property location User's location (defaults to empty string if not provided)
 * @property website User's website URL (null if not set)
 * @property createdAt Timestamp when the user account was created
 * @property updatedAt Timestamp when the user profile was last updated (null if never updated)
 */
data class User(
    val id: Long,
    val email: String,
    val username: String,
    val name: String,
    val bio: String,
    val avatarUrl: String?,
    val location: String,
    val website: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)
