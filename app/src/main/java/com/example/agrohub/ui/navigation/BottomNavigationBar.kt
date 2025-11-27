package com.example.agrohub.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * Bottom navigation bar for the AgroHub application.
 * Displays navigation items with icons and labels, highlighting the active item.
 * 
 * Requirements: 10.1, 10.2, 10.4, 10.5
 * 
 * @param currentRoute The currently active route
 * @param onNavigate Callback when a navigation item is selected
 * @param modifier Optional modifier for the navigation bar
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = AgroHubColors.White,
        tonalElevation = 8.dp
    ) {
        navigationItems.forEach { item ->
            val isSelected = currentRoute == item.route
            
            // Animate color change for highlighting
            val iconColor by animateColorAsState(
                targetValue = if (isSelected) AgroHubColors.DeepGreen else AgroHubColors.CharcoalText.copy(alpha = 0.6f),
                animationSpec = tween(durationMillis = 300),
                label = "iconColor"
            )
            
            val labelColor by animateColorAsState(
                targetValue = if (isSelected) AgroHubColors.DeepGreen else AgroHubColors.CharcoalText.copy(alpha = 0.6f),
                animationSpec = tween(durationMillis = 300),
                label = "labelColor"
            )
            
            // Animate icon scale for highlighting
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1.0f,
                animationSpec = tween(durationMillis = 300),
                label = "iconScale"
            )
            
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = iconColor,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = AgroHubTypography.Caption,
                        color = labelColor
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AgroHubColors.DeepGreen,
                    selectedTextColor = AgroHubColors.DeepGreen,
                    unselectedIconColor = AgroHubColors.CharcoalText.copy(alpha = 0.6f),
                    unselectedTextColor = AgroHubColors.CharcoalText.copy(alpha = 0.6f),
                    indicatorColor = AgroHubColors.LightGreen
                )
            )
        }
    }
}

/**
 * Navigation item data class
 */
private data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

/**
 * List of navigation items for the bottom navigation bar
 */
private val navigationItems = listOf(
    NavigationItem(
        route = Routes.HOME,
        icon = AgroHubIcons.Home,
        label = "Home"
    ),
    NavigationItem(
        route = Routes.SCAN,
        icon = AgroHubIcons.Scan,
        label = "Scan"
    ),
    NavigationItem(
        route = Routes.WEATHER,
        icon = AgroHubIcons.Weather,
        label = "Weather"
    ),
    NavigationItem(
        route = Routes.FARM,
        icon = AgroHubIcons.Farm,
        label = "Farm"
    ),
    NavigationItem(
        route = Routes.MARKET,
        icon = AgroHubIcons.Market,
        label = "Market"
    )
)
