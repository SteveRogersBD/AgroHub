package com.example.agrohub.data.remote.api

import com.example.agrohub.data.remote.dto.CreatePostRequestDto
import com.example.agrohub.data.remote.dto.PagedResponseDto
import com.example.agrohub.data.remote.dto.PostDto
import com.example.agrohub.data.remote.dto.UpdatePostRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service for post endpoints.
 * 
 * Provides methods for post CRUD operations.
 */
interface PostApiService {
    
    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequestDto): PostDto
    
    @PUT("posts/{id}")
    suspend fun updatePost(
        @Path("id") postId: Long,
        @Body request: UpdatePostRequestDto
    ): PostDto
    
    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") postId: Long)
    
    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") postId: Long): PostDto
    
    @GET("posts/user/{userId}")
    suspend fun getUserPosts(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedResponseDto<PostDto>
}
