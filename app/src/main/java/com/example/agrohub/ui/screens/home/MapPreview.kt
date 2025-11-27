package com.example.agrohub.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubShapes
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography
import com.google.android.gms.maps.model.LatLng

/**
 * MapPreview - Displays a preview of farm locations on a map
 * 
 * Note: This is a placeholder implementation showing a map icon.
 * In a full implementation, this would integrate with Google Maps Compose
 * to show actual farm locations on an interactive map.
 * 
 * Requirements: 2.6
 * 
 * @param farmLocations List of farm location coordinates
 */
@Composable
fun MapPreview(farmLocations: List<LatLng>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        // Section Title
        Text(
            text = "Farm Locations",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
        
        // Map Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = AgroHubShapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = AgroHubColors.LightGreen
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(AgroHubColors.LightGreen),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
                ) {
                    Icon(
                        imageVector = AgroHubIcons.Location,
                        contentDescription = "Map showing ${farmLocations.size} farm locations",
                        tint = AgroHubColors.DeepGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Text(
                        text = "${farmLocations.size} Farm${if (farmLocations.size != 1) "s" else ""} Located",
                        style = AgroHubTypography.Body,
                        color = AgroHubColors.DeepGreen,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "Tap to view on map",
                        style = AgroHubTypography.Caption,
                        color = AgroHubColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
