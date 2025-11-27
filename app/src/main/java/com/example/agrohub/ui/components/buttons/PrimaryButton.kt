package com.example.agrohub.ui.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubShapes
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * Primary button component with text, optional icon, and loading state
 * Provides ripple effects and scale feedback animations
 * 
 * @param text Button text label
 * @param onClick Click handler
 * @param modifier Modifier for customization
 * @param icon Optional leading icon
 * @param isLoading Whether button is in loading state
 * @param enabled Whether button is enabled
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    // Scale animation for press feedback
    val scale by animateFloatAsState(
        targetValue = if (enabled && !isLoading) 1f else 0.95f,
        label = "buttonScale"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = AgroHubColors.DeepGreen,
            contentColor = AgroHubColors.White,
            disabledContainerColor = AgroHubColors.LightGreen,
            disabledContentColor = AgroHubColors.TextSecondary
        ),
        shape = AgroHubShapes.medium,
        contentPadding = PaddingValues(
            horizontal = AgroHubSpacing.md,
            vertical = AgroHubSpacing.sm
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AgroHubColors.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(AgroHubSpacing.sm))
                }
                icon != null -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(AgroHubSpacing.sm))
                }
            }
            
            Text(
                text = text,
                style = AgroHubTypography.Body
            )
        }
    }
}
