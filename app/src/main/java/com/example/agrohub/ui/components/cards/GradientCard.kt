package com.example.agrohub.ui.components.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.agrohub.ui.theme.AgroHubShapes
import com.example.agrohub.ui.theme.AgroHubSpacing

/**
 * GradientCard - A card component with gradient background
 * 
 * Features:
 * - Gradient brush background
 * - Rounded corners
 * - Elevation/shadow
 * - Fade-in and scale animations on appearance
 * 
 * @param modifier Modifier for the card
 * @param gradient Brush defining the gradient background
 * @param elevation Elevation for shadow effect
 * @param content Composable content to display inside the card
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush,
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
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
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .scale(scale),
        shape = AgroHubShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = gradient)
                .padding(AgroHubSpacing.md)
        ) {
            content()
        }
    }
}
