package com.example.agrohub.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Like DTOs for backend API integration
 */

@JsonClass(generateAdapter = true)
data class LikeStatusDto(
    @Json(name = "isLiked")
    val isLiked: Boolean
)

@JsonClass(generateAdapter = true)
data class LikeCountDto(
    @Json(name = "count")
    val count: Int
)

@JsonClass(generateAdapter = true)
data class BatchLikeCountRequestDto(
    @Json(name = "postIds")
    val postIds: List<Long>
)

@JsonClass(generateAdapter = true)
data class BatchLikeCountResponseDto(
    @Json(name = "counts")
    val counts: Map<String, Int>
)
