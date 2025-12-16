package com.example.agrohub.data.remote.api

import com.example.agrohub.data.remote.dto.BatchLikeCountRequestDto
import com.example.agrohub.data.remote.dto.BatchLikeCountResponseDto
import com.example.agrohub.data.remote.dto.LikeCountDto
import com.example.agrohub.data.remote.dto.LikeStatusDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit API service for like endpoints.
 * 
 * Provides methods for liking/unliking posts and retrieving like counts.
 */
interface LikeApiService {
    
    @POST("likes/{postId}")
    suspend fun likePost(@Path("postId") postId: Long)
    
    @DELETE("likes/{postId}")
    suspend fun unlikePost(@Path("postId") postId: Long)
    
    @GET("likes/{postId}/check")
    suspend fun checkLikeStatus(@Path("postId") postId: Long): LikeStatusDto
    
    @GET("likes/{postId}/count")
    suspend fun getLikeCount(@Path("postId") postId: Long): LikeCountDto
    
    @POST("likes/batch/counts")
    suspend fun getBatchLikeCounts(@Body request: BatchLikeCountRequestDto): BatchLikeCountResponseDto
}
