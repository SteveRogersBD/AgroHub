package com.example.agrohub.models

/**
 * Data model representing a community post
 */
data class Post(
    val id: String,
    val userName: String,
    val userAvatar: Int,
    val timestamp: String,
    val content: String,
    val images: List<Int>,
    val likes: Int,
    val comments: Int,
    val shares: Int,
    val isLiked: Boolean
)

/**
 * Data model representing a comment on a post
 */
data class Comment(
    val id: String,
    val userName: String,
    val userAvatar: Int,
    val timestamp: String,
    val content: String,
    val likes: Int
)
