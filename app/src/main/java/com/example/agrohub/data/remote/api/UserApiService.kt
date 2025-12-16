package com.example.agrohub.data.remote.api

import com.example.agrohub.data.remote.dto.CreateProfileRequestDto
import com.example.agrohub.data.remote.dto.PagedResponseDto
import com.example.agrohub.data.remote.dto.UpdateProfileRequestDto
import com.example.agrohub.data.remote.dto.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service for user profile endpoints.
 * 
 * Provides methods for profile CRUD operations and user search.
 */
interface UserApiService {
    
    @POST("users")
    suspend fun createProfile(@Body request: CreateProfileRequestDto): UserProfileDto
    
    @PUT("users/{id}")
    suspend fun updateProfile(
        @Path("id") userId: Long,
        @Body request: UpdateProfileRequestDto
    ): UserProfileDto
    
    @GET("users/me")
    suspend fun getCurrentUser(): UserProfileDto
    
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Long): UserProfileDto
    
    @GET("users/username/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): UserProfileDto
    
    @GET("users/search")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedResponseDto<UserProfileDto>
}
