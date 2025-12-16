package com.example.agrohub

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.agrohub.utils.LocationPermissionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * Main activity for the AgroHub application.
 * Sets up the Compose UI with the AgroHubApp root composable.
 * Handles location permission requests on app startup.
 * 
 * Requirements: 10.1, 10.5
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var locationPermissionManager: LocationPermissionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Fine location access granted
                locationPermissionManager.saveLocationPermissionGranted(true)
                fetchAndSaveLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Coarse location access granted
                locationPermissionManager.saveLocationPermissionGranted(true)
                fetchAndSaveLocation()
            }
            else -> {
                // No location access granted
                locationPermissionManager.saveLocationPermissionGranted(false)
                Toast.makeText(
                    this,
                    "Location permission denied. Some features may not work properly.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize location services
        locationPermissionManager = LocationPermissionManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Check and request location permission if not already granted
        checkAndRequestLocationPermission()
        
        setContent {
            AgroHubApp()
        }
    }
    
    private fun checkAndRequestLocationPermission() {
        // Check if permission was already granted and saved
        if (locationPermissionManager.isLocationPermissionGranted()) {
            // Permission already granted, optionally update location
            if (hasLocationPermission()) {
                fetchAndSaveLocation()
            }
            return
        }
        
        // Check if we have permission
        if (hasLocationPermission()) {
            // We have permission but it's not saved yet
            locationPermissionManager.saveLocationPermissionGranted(true)
            fetchAndSaveLocation()
        } else {
            // Request permission
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun fetchAndSaveLocation() {
        if (!hasLocationPermission()) {
            return
        }
        
        try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                location?.let {
                    // Save location to SharedPreferences
                    locationPermissionManager.saveUserLocation(it.latitude, it.longitude)
                }
            }.addOnFailureListener { exception ->
                // Handle failure silently or log
                exception.printStackTrace()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}