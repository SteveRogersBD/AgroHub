package com.example.agrohub.data.remote

import com.example.agrohub.models.NewsResponse
import com.example.agrohub.models.VideoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SerpApiService {
    
    @GET("search")
    suspend fun getYoutubeVideos(
        @Query("engine") engine: String = "youtube",
        @Query("search_query") searchQuery: String = "agriculture",
        @Query("api_key") apiKey: String = "5430f234ebc964dd1ca39b4c98572751c4ddadf23fb5157fce32e354e7aa9811"
    ): VideoResponse
    
    @GET("search")
    suspend fun getNews(
        @Query("engine") engine: String = "google_news",
        @Query("q") query: String = "agriculture",
        @Query("api_key") apiKey: String = "5430f234ebc964dd1ca39b4c98572751c4ddadf23fb5157fce32e354e7aa9811",
        @Query("gl") gl: String = "us"
    ): NewsResponse
}
