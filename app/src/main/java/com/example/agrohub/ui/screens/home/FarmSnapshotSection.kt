package com.example.agrohub.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.agrohub.models.FarmData
import com.example.agrohub.ui.components.cards.CropStatusCard
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * FarmSnapshotSection - Displays crop status cards for all farms
 * 
 * Shows a list of crop status cards with:
 * - Crop image
 * - Health status
 * - Health percentage
 * - Last watered information
 * 
 * Requirements: 2.3
 * 
 * @param farms List of farm data to display
 */
@Composable
fun FarmSnapshotSection(farms: List<FarmData>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        // Section Title
        Text(
            text = "Farm Snapshot",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
        
        // Crop Status Cards
        farms.forEach { farm ->
            CropStatusCard(
                cropName = "${farm.name} - ${farm.cropType}",
                cropImage = android.R.drawable.ic_menu_gallery, // Placeholder image
                healthStatus = farm.healthStatus,
                healthPercentage = farm.healthPercentage / 100f,
                lastWatered = "2 days ago" // Mock data
            )
        }
    }
}
