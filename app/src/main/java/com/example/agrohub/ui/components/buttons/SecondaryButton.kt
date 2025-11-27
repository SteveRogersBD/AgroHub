package com.example.agrohub.ui.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubShapes
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * Secondary button component with outlined style
 * Provides ripple effects and scale feedback animations
 * 
 * @param text Button text label
 * @param onClick Click handler
 * @param modifier Modifier for customization
 * @param enabled Whether button is enabled
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Scale animation for press feedback
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        label = "buttonScale"
    )
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AgroHubColors.White,
            contentColor = AgroHubColors.DeepGreen,
            disabledContainerColor = AgroHubColors.White,
            disabledContentColor = AgroHubColors.TextSecondary
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (enabled) AgroHubColors.DeepGreen else AgroHubColors.TextHint
        ),
        shape = AgroHubShapes.medium,
        contentPadding = PaddingValues(
            horizontal = AgroHubSpacing.md,
            vertical = AgroHubSpacing.sm
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            style = AgroHubTypography.Body
        )
    }
}
