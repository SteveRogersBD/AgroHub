package com.example.agrohub.data.remote.api

import com.example.agrohub.models.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Weather API service for fetching weather data from weatherapi.com
 */
interface WeatherApiService {
    
    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 7,
        @Query("aqi") includeAqi: String = "yes",
        @Query("alerts") includeAlerts: String = "yes"
    ): WeatherResponse
}
