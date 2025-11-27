package com.example.agrohub.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.example.agrohub.ui.components.cards.StatCard
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * QuickStatsSection - Displays quick statistics cards
 * 
 * Shows three stat cards with gradient backgrounds:
 * - Total Farms
 * - Active Crops
 * - Pending Tasks
 * 
 * Requirements: 2.2
 * 
 * @param totalFarms Number of total farms
 * @param activeCrops Number of active crops
 * @param pendingTasks Number of pending tasks
 */
@Composable
fun QuickStatsSection(
    totalFarms: Int,
    activeCrops: Int,
    pendingTasks: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        // Section Title
        Text(
            text = "Quick Stats",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
        
        // Stats Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
        ) {
            // Total Farms Card
            StatCard(
                title = "Total Farms",
                value = totalFarms.toString(),
                icon = AgroHubIcons.Field,
                gradient = Brush.linearGradient(
                    colors = listOf(
                        AgroHubColors.DeepGreen,
                        AgroHubColors.MediumGreen
                    )
                ),
                modifier = Modifier.weight(1f)
            )
            
            // Active Crops Card
            StatCard(
                title = "Active Crops",
                value = activeCrops.toString(),
                icon = AgroHubIcons.Plant,
                gradient = Brush.linearGradient(
                    colors = listOf(
                        AgroHubColors.SkyBlue,
                        AgroHubColors.DeepGreen
                    )
                ),
                modifier = Modifier.weight(1f)
            )
            
            // Pending Tasks Card
            StatCard(
                title = "Pending Tasks",
                value = pendingTasks.toString(),
                icon = AgroHubIcons.Check,
                gradient = Brush.linearGradient(
                    colors = listOf(
                        AgroHubColors.GoldenHarvest,
                        AgroHubColors.DeepGreen
                    )
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
