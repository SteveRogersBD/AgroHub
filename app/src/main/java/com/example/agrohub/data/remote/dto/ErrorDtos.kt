package com.example.agrohub.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Error response DTOs for backend API integration
 */

@JsonClass(generateAdapter = true)
data class ErrorResponseDto(
    @Json(name = "timestamp")
    val timestamp: String,
    @Json(name = "status")
    val status: Int,
    @Json(name = "error")
    val error: String,
    @Json(name = "message")
    val message: String,
    @Json(name = "path")
    val path: String
)
