package com.example.agrohub.models

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Enum representing risk levels for weather conditions
 */
enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

/**
 * Data model representing a daily weather forecast
 */
data class DailyForecast(
    val date: String,
    val icon: ImageVector,
    val tempHigh: String,
    val tempLow: String,
    val riskLevel: RiskLevel,
    val condition: String
)

/**
 * Data model representing a weather alert
 */
data class WeatherAlert(
    val alertType: String,
    val severity: String,
    val description: String,
    val timestamp: String,
    val icon: ImageVector
)
