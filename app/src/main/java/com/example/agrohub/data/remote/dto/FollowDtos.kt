package com.example.agrohub.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Follow DTOs for backend API integration
 */

@JsonClass(generateAdapter = true)
data class FollowStatusDto(
    @Json(name = "isFollowing")
    val isFollowing: Boolean
)

@JsonClass(generateAdapter = true)
data class FollowStatsDto(
    @Json(name = "followersCount")
    val followersCount: Int,
    @Json(name = "followingCount")
    val followingCount: Int
)
