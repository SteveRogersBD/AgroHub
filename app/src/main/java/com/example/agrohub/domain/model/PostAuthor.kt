package com.example.agrohub.domain.model

/**
 * Domain model representing the author information for a post.
 * This is a lightweight representation used in feed posts and comments.
 *
 * @property id Unique identifier for the author
 * @property username Author's username
 * @property avatarUrl URL to author's avatar image (null if not set)
 */
data class PostAuthor(
    val id: Long,
    val username: String,
    val avatarUrl: String?
)
