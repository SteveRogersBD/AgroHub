package com.example.agrohub.data.remote.api

import com.example.agrohub.data.remote.dto.LoginRequestDto
import com.example.agrohub.data.remote.dto.LoginResponseDto
import com.example.agrohub.data.remote.dto.RefreshTokenRequestDto
import com.example.agrohub.data.remote.dto.RefreshTokenResponseDto
import com.example.agrohub.data.remote.dto.RegisterRequestDto
import com.example.agrohub.data.remote.dto.RegisterResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API service for authentication endpoints.
 * 
 * Provides methods for user registration, login, and token refresh.
 */
interface AuthApiService {
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): RegisterResponseDto
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto
    
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequestDto): RefreshTokenResponseDto
}
