package com.example.agrohub.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.navigation.Routes
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubShapes
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * QuickActionsGrid - Grid of quick action buttons for navigation
 * 
 * Displays prominent buttons for:
 * - Scan (Disease Detection)
 * - Weather
 * - Community
 * - Market
 * 
 * Requirements: 2.5
 * 
 * @param navController Navigation controller for screen navigation
 */
@Composable
fun QuickActionsGrid(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        // Section Title
        Text(
            text = "Quick Actions",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
        
        // First Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
        ) {
            QuickActionButton(
                icon = AgroHubIcons.Scan,
                label = "Scan",
                onClick = { navController.navigate(Routes.SCAN) },
                modifier = Modifier.weight(1f)
            )
            
            QuickActionButton(
                icon = AgroHubIcons.Weather,
                label = "Weather",
                onClick = { navController.navigate(Routes.WEATHER) },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Second Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
        ) {
            QuickActionButton(
                icon = AgroHubIcons.Community,
                label = "Community",
                onClick = { navController.navigate(Routes.COMMUNITY) },
                modifier = Modifier.weight(1f)
            )
            
            QuickActionButton(
                icon = AgroHubIcons.Market,
                label = "Market",
                onClick = { navController.navigate(Routes.MARKET) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * QuickActionButton - Individual action button component
 * 
 * @param icon Icon to display
 * @param label Label text
 * @param onClick Click handler
 * @param modifier Modifier for the button
 */
@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = AgroHubShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = AgroHubColors.DeepGreen
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$label action button",
                tint = AgroHubColors.White,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = label,
                style = AgroHubTypography.Body,
                color = AgroHubColors.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
