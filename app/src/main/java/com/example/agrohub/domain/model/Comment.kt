package com.example.agrohub.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing a comment on a post.
 *
 * @property id Unique identifier for the comment
 * @property postId ID of the post this comment belongs to
 * @property author Information about the comment author
 * @property content The text content of the comment
 * @property createdAt Timestamp when the comment was created
 * @property updatedAt Timestamp when the comment was last updated (null if never updated)
 */
data class Comment(
    val id: Long,
    val postId: Long,
    val author: CommentAuthor,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)
