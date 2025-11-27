package com.example.agrohub.ui.components.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing

/**
 * Loading indicator components with shimmer effects and progress indicators.
 * Provides visual feedback during loading states.
 * 
 * Requirements: 11.5
 */

/**
 * Circular progress indicator with AgroHub theme colors
 * 
 * @param modifier Modifier for styling
 * @param size Size of the circular indicator
 */
@Composable
fun AgroHubCircularProgress(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(AgroHubSpacing.lg),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = AgroHubColors.DeepGreen,
            strokeWidth = 4.dp
        )
    }
}

/**
 * Linear progress indicator with AgroHub theme colors
 * 
 * @param modifier Modifier for styling
 * @param progress Optional progress value (0.0 to 1.0). If null, shows indeterminate progress
 */
@Composable
fun AgroHubLinearProgress(
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    if (progress != null) {
        LinearProgressIndicator(
            progress = progress,
            modifier = modifier
                .fillMaxWidth()
                .height(4.dp),
            color = AgroHubColors.DeepGreen,
            trackColor = AgroHubColors.LightGreen
        )
    } else {
        LinearProgressIndicator(
            modifier = modifier
                .fillMaxWidth()
                .height(4.dp),
            color = AgroHubColors.DeepGreen,
            trackColor = AgroHubColors.LightGreen
        )
    }
}

/**
 * Shimmer effect for loading placeholders
 * Creates an animated gradient that moves across the content
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val shimmerColors = listOf(
        AgroHubColors.SurfaceLight.copy(alpha = 0.9f),
        AgroHubColors.SurfaceLight.copy(alpha = 0.3f),
        AgroHubColors.SurfaceLight.copy(alpha = 0.9f)
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation, translateAnimation),
        end = Offset(translateAnimation + 200f, translateAnimation + 200f)
    )
}

/**
 * Shimmer loading box - rectangular placeholder with shimmer effect
 * 
 * @param modifier Modifier for styling
 * @param height Height of the shimmer box
 * @param cornerRadius Corner radius for rounded corners
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp,
    cornerRadius: Dp = 8.dp
) {
    val shimmerBrush = rememberShimmerBrush()
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush)
    )
}

/**
 * Shimmer loading circle - circular placeholder with shimmer effect
 * 
 * @param modifier Modifier for styling
 * @param size Size of the shimmer circle
 */
@Composable
fun ShimmerCircle(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val shimmerBrush = rememberShimmerBrush()
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(shimmerBrush)
    )
}

/**
 * Card loading placeholder with shimmer effect
 * Simulates a card with title and content areas
 * 
 * @param modifier Modifier for styling
 */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AgroHubSpacing.md)
    ) {
        // Title shimmer
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(0.6f),
            height = 24.dp,
            cornerRadius = 4.dp
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
        
        // Content shimmer lines
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            height = 16.dp,
            cornerRadius = 4.dp
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
        
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(0.8f),
            height = 16.dp,
            cornerRadius = 4.dp
        )
    }
}

/**
 * List item loading placeholder with shimmer effect
 * Simulates a list item with icon and text
 * 
 * @param modifier Modifier for styling
 */
@Composable
fun ShimmerListItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(AgroHubSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon shimmer
        ShimmerCircle(size = 48.dp)
        
        Spacer(modifier = Modifier.width(AgroHubSpacing.md))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Title shimmer
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.7f),
                height = 20.dp,
                cornerRadius = 4.dp
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
            
            // Subtitle shimmer
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.5f),
                height = 16.dp,
                cornerRadius = 4.dp
            )
        }
    }
}

/**
 * Full screen loading indicator
 * Centers a circular progress indicator on the screen
 * 
 * @param modifier Modifier for styling
 */
@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AgroHubCircularProgress()
    }
}

/**
 * Loading list - shows multiple shimmer list items
 * Useful for loading states in scrollable lists
 * 
 * @param modifier Modifier for styling
 * @param itemCount Number of shimmer items to show
 */
@Composable
fun LoadingList(
    modifier: Modifier = Modifier,
    itemCount: Int = 5
) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(itemCount) {
            ShimmerListItem()
            if (it < itemCount - 1) {
                Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            }
        }
    }
}
