package com.example.agrohub.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing an enriched post in the user's feed.
 * This includes additional metadata like like counts, comment counts, and author information.
 *
 * @property id Unique identifier for the post
 * @property author Information about the post author
 * @property content The text content of the post
 * @property mediaUrl URL to attached media (image/video), null if no media
 * @property likeCount Number of likes on the post
 * @property commentCount Number of comments on the post
 * @property isLikedByCurrentUser Whether the current user has liked this post
 * @property createdAt Timestamp when the post was created
 * @property updatedAt Timestamp when the post was last updated (null if never updated)
 */
data class FeedPost(
    val id: Long,
    val author: PostAuthor,
    val content: String,
    val mediaUrl: String?,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByCurrentUser: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)
