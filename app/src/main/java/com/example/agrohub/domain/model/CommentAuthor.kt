package com.example.agrohub.domain.model

/**
 * Domain model representing the author information for a comment.
 * This is a lightweight representation used in comment displays.
 *
 * @property id Unique identifier for the author
 * @property username Author's username
 * @property avatarUrl URL to author's avatar image (null if not set)
 */
data class CommentAuthor(
    val id: Long,
    val username: String,
    val avatarUrl: String?
)
