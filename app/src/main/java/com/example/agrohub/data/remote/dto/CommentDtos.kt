package com.example.agrohub.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Comment DTOs for backend API integration
 */

@JsonClass(generateAdapter = true)
data class CommentDto(
    @Json(name = "id")
    val id: Long,
    @Json(name = "postId")
    val postId: Long,
    @Json(name = "userId")
    val userId: Long,
    @Json(name = "username")
    val username: String,
    @Json(name = "userAvatarUrl")
    val userAvatarUrl: String?,
    @Json(name = "content")
    val content: String,
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "updatedAt")
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class CreateCommentRequestDto(
    @Json(name = "postId")
    val postId: Long,
    @Json(name = "content")
    val content: String
)

@JsonClass(generateAdapter = true)
data class UpdateCommentRequestDto(
    @Json(name = "content")
    val content: String
)
