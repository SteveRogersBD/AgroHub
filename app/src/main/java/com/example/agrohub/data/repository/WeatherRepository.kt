package com.example.agrohub.data.repository

import com.example.agrohub.data.remote.api.WeatherApiService
import com.example.agrohub.models.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for weather data operations
 */
class WeatherRepository(
    private val weatherApiService: WeatherApiService,
    private val apiKey: String
) {
    
    /**
     * Fetch 7-day weather forecast for a location
     */
    suspend fun getForecast(location: String): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        try {
            val response = weatherApiService.getForecast(
                apiKey = apiKey,
                location = location,
                days = 7,
                includeAqi = "yes",
                includeAlerts = "yes"
            )
            
            // Debug logging
            println("Weather API Response: Location=${response.location?.name}, Forecast days=${response.forecast?.forecastday?.size}")
            response.forecast?.forecastday?.forEachIndexed { index, day ->
                println("  Day $index: ${day.date} - ${day.day?.condition?.text}")
            }
            
            Result.success(response)
        } catch (e: Exception) {
            println("Weather API Error: ${e.message}")
            Result.failure(e)
        }
    }
}
