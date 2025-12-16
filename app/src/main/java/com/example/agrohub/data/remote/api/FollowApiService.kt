package com.example.agrohub.data.remote.api

import com.example.agrohub.data.remote.dto.FollowStatsDto
import com.example.agrohub.data.remote.dto.FollowStatusDto
import com.example.agrohub.data.remote.dto.PagedResponseDto
import com.example.agrohub.data.remote.dto.UserProfileDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service for follow/unfollow endpoints.
 * 
 * Provides methods for managing user follow relationships.
 */
interface FollowApiService {
    
    @POST("follows/{userId}")
    suspend fun followUser(@Path("userId") userId: Long)
    
    @DELETE("follows/{userId}")
    suspend fun unfollowUser(@Path("userId") userId: Long)
    
    @GET("follows/check/{userId}")
    suspend fun checkFollowStatus(@Path("userId") userId: Long): FollowStatusDto
    
    @GET("follows/{userId}/stats")
    suspend fun getFollowStats(@Path("userId") userId: Long): FollowStatsDto
    
    @GET("follows/{userId}/followers")
    suspend fun getFollowers(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedResponseDto<UserProfileDto>
    
    @GET("follows/{userId}/following")
    suspend fun getFollowing(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedResponseDto<UserProfileDto>
}
