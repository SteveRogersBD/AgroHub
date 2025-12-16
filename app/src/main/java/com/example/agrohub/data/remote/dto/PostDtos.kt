package com.example.agrohub.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Post DTOs for backend API integration
 */

@JsonClass(generateAdapter = true)
data class PostDto(
    @Json(name = "id")
    val id: Long,
    @Json(name = "userId")
    val userId: Long,
    @Json(name = "content")
    val content: String? = "",  // Make nullable with default
    @Json(name = "mediaUrl")
    val mediaUrl: String? = null,
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "updatedAt")
    val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class CreatePostRequestDto(
    @Json(name = "content")
    val content: String,
    @Json(name = "mediaUrl")
    val mediaUrl: String?
)

@JsonClass(generateAdapter = true)
data class UpdatePostRequestDto(
    @Json(name = "content")
    val content: String,
    @Json(name = "mediaUrl")
    val mediaUrl: String?
)

@JsonClass(generateAdapter = true)
data class FeedPostDto(
    @Json(name = "id")
    val id: Long,
    @Json(name = "userId")
    val userId: Long,
    @Json(name = "username")
    val username: String? = "Unknown",
    @Json(name = "userAvatarUrl")
    val userAvatarUrl: String? = null,
    @Json(name = "content")
    val content: String? = "",  // Make nullable with default
    @Json(name = "mediaUrl")
    val mediaUrl: String? = null,
    @Json(name = "likeCount")
    val likeCount: Int = 0,
    @Json(name = "commentCount")
    val commentCount: Int = 0,
    @Json(name = "likedByCurrentUser")
    val likedByCurrentUser: Boolean = false,
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "updatedAt")
    val updatedAt: String? = null
)
