package com.example.agrohub.ui.components.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubShapes
import com.example.agrohub.ui.theme.AgroHubSpacing

/**
 * CropStatusCard - A card component displaying crop status information
 * 
 * Features:
 * - Crop image display
 * - Health status indicator with color coding
 * - Health percentage progress bar
 * - Last watered timestamp
 * - Rounded corners and elevation
 * - Fade-in and scale animations on appearance
 * 
 * @param cropName Name of the crop
 * @param cropImage Resource ID for the crop image
 * @param healthStatus Health status text (e.g., "Healthy", "Warning", "Critical")
 * @param healthPercentage Health percentage (0.0 to 1.0)
 * @param lastWatered Last watered timestamp text
 * @param modifier Modifier for the card
 */
@Composable
fun CropStatusCard(
    cropName: String,
    cropImage: Int,
    healthStatus: String,
    healthPercentage: Float,
    lastWatered: String,
    modifier: Modifier = Modifier
) {
    // Animation state for fade-in and scale
    var visible by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    // Determine health color based on status
    val healthColor = when {
        healthStatus.contains("Healthy", ignoreCase = true) -> AgroHubColors.HealthyGreen
        healthStatus.contains("Warning", ignoreCase = true) -> AgroHubColors.WarningYellow
        healthStatus.contains("Critical", ignoreCase = true) -> AgroHubColors.CriticalRed
        healthPercentage >= 0.7f -> AgroHubColors.HealthyGreen
        healthPercentage >= 0.4f -> AgroHubColors.WarningYellow
        else -> AgroHubColors.CriticalRed
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .scale(scale),
        shape = AgroHubShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
        ) {
            // Crop Image
            Image(
                painter = painterResource(id = cropImage),
                contentDescription = cropName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(AgroHubShapes.medium),
                contentScale = ContentScale.Crop
            )
            
            // Crop Information
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
            ) {
                // Crop Name
                Text(
                    text = cropName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgroHubColors.TextPrimary
                )
                
                // Health Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.xs)
                ) {
                    Text(
                        text = healthStatus,
                        fontSize = 14.sp,
                        color = healthColor,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(healthPercentage * 100).toInt()}%",
                        fontSize = 14.sp,
                        color = AgroHubColors.TextSecondary
                    )
                }
                
                // Health Progress Bar
                LinearProgressIndicator(
                    progress = { healthPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(AgroHubShapes.small),
                    color = healthColor,
                    trackColor = AgroHubColors.SurfaceLight
                )
                
                // Last Watered
                Text(
                    text = "Last watered: $lastWatered",
                    fontSize = 12.sp,
                    color = AgroHubColors.TextSecondary
                )
            }
        }
    }
}
