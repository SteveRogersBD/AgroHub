package com.example.agrohub.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * User DTOs for backend API integration
 */

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    @Json(name = "id")
    val id: Long,
    @Json(name = "email")
    val email: String,
    @Json(name = "username")
    val username: String,
    @Json(name = "name")
    val name: String?,
    @Json(name = "bio")
    val bio: String?,
    @Json(name = "avatarUrl")
    val avatarUrl: String?,
    @Json(name = "location")
    val location: String?,
    @Json(name = "website")
    val website: String?,
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "updatedAt")
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class CreateProfileRequestDto(
    @Json(name = "name")
    val name: String?,
    @Json(name = "bio")
    val bio: String?,
    @Json(name = "avatarUrl")
    val avatarUrl: String?,
    @Json(name = "location")
    val location: String?,
    @Json(name = "website")
    val website: String?
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequestDto(
    @Json(name = "name")
    val name: String?,
    @Json(name = "bio")
    val bio: String?,
    @Json(name = "avatarUrl")
    val avatarUrl: String?,
    @Json(name = "location")
    val location: String?,
    @Json(name = "website")
    val website: String?
)
