package com.example.agrohub.domain.model

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

/**
 * Domain model representing a farm field with its boundaries
 */
data class Field(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val points: List<FieldPoint> = emptyList(),
    val centerPoint: FieldPoint? = null,
    val centerAddress: String = "",
    val areaInSquareMeters: Double = 0.0,
    val tasks: List<FieldTask> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate the center point of the field
     */
    fun calculateCenter(): FieldPoint? {
        if (points.isEmpty()) return null
        
        val avgLat = points.map { it.latitude }.average()
        val avgLng = points.map { it.longitude }.average()
        return FieldPoint(avgLat, avgLng)
    }
    
    /**
     * Calculate the area of the field in square meters using the Shoelace formula
     */
    fun calculateArea(): Double {
        if (points.size < 3) return 0.0
        
        var area = 0.0
        val n = points.size
        
        for (i in 0 until n) {
            val j = (i + 1) % n
            val lat1 = points[i].latitude
            val lng1 = points[i].longitude
            val lat2 = points[j].latitude
            val lng2 = points[j].longitude
            
            area += lng1 * lat2 - lng2 * lat1
        }
        
        area = abs(area) / 2.0
        
        // Convert from degrees to square meters (approximate)
        // At equator: 1 degree latitude ≈ 111,320 meters
        // 1 degree longitude varies by latitude
        val avgLat = points.map { it.latitude }.average()
        val metersPerDegreeLat = 111320.0
        val metersPerDegreeLng = 111320.0 * cos(Math.toRadians(avgLat))
        
        return area * metersPerDegreeLat * metersPerDegreeLng
    }
}

/**
 * Represents a point in the field boundary
 */
data class FieldPoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    fun toLatLng(): LatLng = LatLng(latitude, longitude)
    
    companion object {
        fun fromLatLng(latLng: LatLng): FieldPoint {
            return FieldPoint(latLng.latitude, latLng.longitude)
        }
    }
}

/**
 * Represents a task associated with a field
 */
data class FieldTask(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
