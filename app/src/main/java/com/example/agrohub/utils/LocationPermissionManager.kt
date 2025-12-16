package com.example.agrohub.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manager for handling location permission state in SharedPreferences.
 * Stores whether location permission has been granted to avoid repeated requests.
 */
class LocationPermissionManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    /**
     * Save location permission granted status
     */
    fun saveLocationPermissionGranted(granted: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_LOCATION_PERMISSION_GRANTED, granted)
            apply()
        }
    }
    
    /**
     * Check if location permission has been granted before
     */
    fun isLocationPermissionGranted(): Boolean {
        return sharedPreferences.getBoolean(KEY_LOCATION_PERMISSION_GRANTED, false)
    }
    
    /**
     * Save user's location coordinates
     */
    fun saveUserLocation(latitude: Double, longitude: Double) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_LATITUDE, latitude.toString())
            putString(KEY_USER_LONGITUDE, longitude.toString())
            apply()
        }
    }
    
    /**
     * Get saved user location
     * @return Pair of latitude and longitude, or null if not saved
     */
    fun getUserLocation(): Pair<Double, Double>? {
        val lat = sharedPreferences.getString(KEY_USER_LATITUDE, null)?.toDoubleOrNull()
        val lon = sharedPreferences.getString(KEY_USER_LONGITUDE, null)?.toDoubleOrNull()
        
        return if (lat != null && lon != null) {
            Pair(lat, lon)
        } else {
            null
        }
    }
    
    companion object {
        private const val PREFS_NAME = "agrohub_location_prefs"
        private const val KEY_LOCATION_PERMISSION_GRANTED = "location_permission_granted"
        private const val KEY_USER_LATITUDE = "user_latitude"
        private const val KEY_USER_LONGITUDE = "user_longitude"
    }
}
