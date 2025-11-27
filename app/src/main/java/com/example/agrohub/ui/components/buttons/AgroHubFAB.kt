package com.example.agrohub.ui.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubShapes

/**
 * Floating Action Button component for primary actions
 * Provides ripple effects and scale feedback animations
 * 
 * @param icon Icon to display in the FAB
 * @param onClick Click handler
 * @param modifier Modifier for customization
 * @param contentDescription Accessibility description for the icon
 */
@Composable
fun AgroHubFAB(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    // Scale animation for press feedback
    val scale by animateFloatAsState(
        targetValue = 1f,
        label = "fabScale"
    )
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        containerColor = AgroHubColors.DeepGreen,
        contentColor = AgroHubColors.White,
        shape = AgroHubShapes.large,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp,
            hoveredElevation = 8.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}
