package com.example.agrohub.ui.screens.farm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.navigation.Routes
import com.example.agrohub.ui.theme.AgroHubColors
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/**
 * FieldMapScreen - Full screen map view for field tracking
 * 
 * Displays:
 * - Interactive Google Map showing farm fields
 * - Field markers and boundaries
 * - Floating chatbot button for agriculture assistance
 * 
 * @param navController Navigation controller for screen navigation
 */
@Composable
fun FieldMapScreen(navController: NavController) {
    // Default location (you can change this to user's location or farm location)
    val defaultLocation = LatLng(28.6139, 77.2090) // Delhi, India - change as needed
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }
    
    // Sample farm locations
    val farmLocations = remember {
        listOf(
            LatLng(28.6139, 77.2090),
            LatLng(28.6239, 77.2190),
            LatLng(28.6039, 77.1990)
        )
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.HYBRID
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = true,
                compassEnabled = true
            )
        ) {
            // Add markers for each farm location
            farmLocations.forEachIndexed { index, location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Farm ${index + 1}",
                    snippet = "Tap for details"
                )
            }
        }
        
        // Floating Chatbot Button
        FloatingActionButton(
            onClick = { navController.navigate(Routes.CHAT) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = CircleShape,
            containerColor = AgroHubColors.DeepGreen,
            contentColor = AgroHubColors.White
        ) {
            Icon(
                imageVector = AgroHubIcons.Chat,
                contentDescription = "Agriculture Assistant",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
