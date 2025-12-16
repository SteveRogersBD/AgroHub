package com.example.agrohub.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Notification DTOs for backend API integration
 */

@JsonClass(generateAdapter = true)
data class NotificationDto(
    @Json(name = "id")
    val id: Long,
    @Json(name = "userId")
    val userId: Long,
    @Json(name = "type")
    val type: String, // LIKE, COMMENT, FOLLOW
    @Json(name = "actorId")
    val actorId: Long,
    @Json(name = "actorUsername")
    val actorUsername: String,
    @Json(name = "actorAvatarUrl")
    val actorAvatarUrl: String?,
    @Json(name = "postId")
    val postId: Long?,
    @Json(name = "message")
    val message: String,
    @Json(name = "isRead")
    val isRead: Boolean,
    @Json(name = "createdAt")
    val createdAt: String
)
