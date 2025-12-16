package com.example.agrohub.data.remote.api

import com.example.agrohub.data.remote.dto.FeedPostDto
import com.example.agrohub.data.remote.dto.PagedResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API service for feed endpoints.
 * 
 * Provides methods for retrieving personalized feed.
 */
interface FeedApiService {
    
    @GET("feed")
    suspend fun getPersonalizedFeed(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedResponseDto<FeedPostDto>
}
