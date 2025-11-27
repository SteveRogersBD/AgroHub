package com.example.agrohub.models

import com.google.android.gms.maps.model.LatLng

/**
 * Data model representing a farm with its details and status
 */
data class FarmData(
    val id: String,
    val name: String,
    val cropType: String,
    val location: LatLng,
    val size: Double,
    val healthStatus: String,
    val healthPercentage: Float
)

/**
 * Data model for quick statistics displayed on the dashboard
 */
data class QuickStats(
    val totalFarms: Int,
    val activeCrops: Int,
    val pendingTasks: Int
)
