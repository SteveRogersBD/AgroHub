package com.example.agrohub.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Authentication DTOs for backend API integration
 */

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    @Json(name = "email")
    val email: String,
    @Json(name = "username")
    val username: String,
    @Json(name = "password")
    val password: String
)

// Backend returns LoginResponse for registration
typealias RegisterResponseDto = LoginResponseDto

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @Json(name = "emailOrUsername")
    val emailOrUsername: String,
    @Json(name = "password")
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    @Json(name = "accessToken")
    val accessToken: String,
    @Json(name = "refreshToken")
    val refreshToken: String,
    @Json(name = "userId")
    val userId: Long,
    @Json(name = "email")
    val email: String,
    @Json(name = "username")
    val username: String,
    @Json(name = "role")
    val role: String
) {
    // Provide default values for fields that frontend expects but backend doesn't return
    val tokenType: String = "Bearer"
    val expiresIn: Long = 3600000L // 1 hour in milliseconds as default
}

@JsonClass(generateAdapter = true)
data class RefreshTokenRequestDto(
    @Json(name = "refreshToken")
    val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class RefreshTokenResponseDto(
    @Json(name = "accessToken")
    val accessToken: String,
    @Json(name = "tokenType")
    val tokenType: String,
    @Json(name = "expiresIn")
    val expiresIn: Long
)
