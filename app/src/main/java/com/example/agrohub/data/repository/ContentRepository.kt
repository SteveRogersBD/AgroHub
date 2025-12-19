package com.example.agrohub.data.repository

import com.example.agrohub.data.remote.SerpApiService
import com.example.agrohub.models.NewsResult
import com.example.agrohub.models.VideoResult

class ContentRepository(private val apiService: SerpApiService) {
    
    suspend fun getAgricultureVideos(): Result<List<VideoResult>> {
        return try {
            val response = apiService.getYoutubeVideos()
            Result.success(response.videoResults ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAgricultureNews(): Result<List<NewsResult>> {
        return try {
            val response = apiService.getNews()
            Result.success(response.newsResults ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
