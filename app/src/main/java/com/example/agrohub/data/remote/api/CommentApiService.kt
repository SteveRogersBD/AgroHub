package com.example.agrohub.data.remote.api

import com.example.agrohub.data.remote.dto.CommentDto
import com.example.agrohub.data.remote.dto.CreateCommentRequestDto
import com.example.agrohub.data.remote.dto.PagedResponseDto
import com.example.agrohub.data.remote.dto.UpdateCommentRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service for comment endpoints.
 * 
 * Provides methods for comment CRUD operations.
 */
interface CommentApiService {
    
    @POST("comments")
    suspend fun createComment(@Body request: CreateCommentRequestDto): CommentDto
    
    @PUT("comments/{id}")
    suspend fun updateComment(
        @Path("id") commentId: Long,
        @Body request: UpdateCommentRequestDto
    ): CommentDto
    
    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") commentId: Long)
    
    @GET("comments/post/{postId}")
    suspend fun getPostComments(
        @Path("postId") postId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedResponseDto<CommentDto>
}
