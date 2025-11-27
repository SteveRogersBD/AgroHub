package com.example.agrohub.ui.utils

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import kotlinx.coroutines.delay

/**
 * AnimationUtils - Centralized animation definitions and utilities for AgroHub
 * 
 * Provides reusable animation definitions following the design system:
 * - Animation durations: 200-400ms for most interactions
 * - Smooth easing curves
 * - Consistent animation patterns across the app
 * 
 * Requirements: 11.1, 11.2, 11.4, 11.6
 */
object AnimationUtils {
    
    // ============================================
    // Animation Durations (200-400ms range)
    // ============================================
    
    /**
     * Fast animation duration for quick feedback (200ms)
     */
    const val DURATION_FAST = 200
    
    /**
     * Standard animation duration for most interactions (300ms)
     */
    const val DURATION_STANDARD = 300
    
    /**
     * Slow animation duration for emphasis (400ms)
     */
    const val DURATION_SLOW = 400
    
    /**
     * Stagger delay between list items (50ms)
     */
    const val STAGGER_DELAY = 50L
    
    // ============================================
    // Easing Curves
    // ============================================
    
    /**
     * Standard easing curve for most animations
     */
    val EASING_STANDARD: Easing = FastOutSlowInEasing
    
    /**
     * Emphasized easing curve for important transitions
     */
    val EASING_EMPHASIZED: Easing = LinearOutSlowInEasing
    
    // ============================================
    // Fade-In Animation for Cards
    // ============================================
    
    /**
     * Creates a fade-in enter transition for cards
     * Duration: 300ms
     * 
     * Requirements: 11.1
     */
    fun fadeInEnter(durationMillis: Int = DURATION_STANDARD): EnterTransition {
        return fadeIn(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = EASING_STANDARD
            )
        )
    }
    
    /**
     * Creates a fade-out exit transition for cards
     * Duration: 200ms
     */
    fun fadeOutExit(durationMillis: Int = DURATION_FAST): ExitTransition {
        return fadeOut(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = EASING_STANDARD
            )
        )
    }
    
    // ============================================
    // Scale Animation for Cards
    // ============================================
    
    /**
     * Creates a scale-in enter transition for cards
     * Scales from 95% to 100%
     * Duration: 300ms
     * 
     * Requirements: 11.1
     */
    fun scaleInEnter(
        initialScale: Float = 0.95f,
        durationMillis: Int = DURATION_STANDARD
    ): EnterTransition {
        return scaleIn(
            initialScale = initialScale,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = EASING_STANDARD
            )
        )
    }
    
    /**
     * Creates a scale-out exit transition for cards
     * Scales from 100% to 95%
     * Duration: 200ms
     */
    fun scaleOutExit(
        targetScale: Float = 0.95f,
        durationMillis: Int = DURATION_FAST
    ): ExitTransition {
        return scaleOut(
            targetScale = targetScale,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = EASING_STANDARD
            )
        )
    }
    
    /**
     * Combined fade-in and scale-in animation for cards
     * This is the standard card appearance animation
     * 
     * Requirements: 11.1
     */
    fun cardEnterAnimation(durationMillis: Int = DURATION_STANDARD): EnterTransition {
        return fadeInEnter(durationMillis) + scaleInEnter(durationMillis = durationMillis)
    }
    
    /**
     * Combined fade-out and scale-out animation for cards
     */
    fun cardExitAnimation(durationMillis: Int = DURATION_FAST): ExitTransition {
        return fadeOutExit(durationMillis) + scaleOutExit(durationMillis = durationMillis)
    }
    
    // ============================================
    // Slide-Up Animation for Messages
    // ============================================
    
    /**
     * Creates a slide-up enter transition for messages
     * Slides from 25% below to final position with fade-in
     * Duration: 300ms
     * 
     * Requirements: 11.2, 5.5
     */
    fun slideUpEnter(durationMillis: Int = DURATION_STANDARD): EnterTransition {
        return slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight / 4 },
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = EASING_STANDARD
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = EASING_STANDARD
            )
        )
    }
    
    /**
     * Creates a slide-down exit transition for messages
     * Duration: 200ms
     */
    fun slideDownExit(durationMillis: Int = DURATION_FAST): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight / 4 },
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = EASING_STANDARD
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = EASING_STANDARD
            )
        )
    }
    
    // ============================================
    // Staggered List Animations
    // ============================================
    
    /**
     * Calculates the delay for staggered list item animations
     * Each item is delayed by STAGGER_DELAY (50ms) more than the previous
     * 
     * Requirements: 11.2
     * 
     * @param index The index of the item in the list
     * @param maxDelay Maximum delay to prevent very long delays for large lists
     * @return Delay in milliseconds
     */
    fun calculateStaggerDelay(index: Int, maxDelay: Long = 500L): Long {
        return (index * STAGGER_DELAY).coerceAtMost(maxDelay)
    }
    
    /**
     * Creates a staggered enter transition for list items
     * Combines fade-in, scale-in, and calculated delay
     * 
     * Requirements: 11.2
     * 
     * @param index The index of the item in the list
     * @param durationMillis Animation duration
     */
    fun staggeredListItemEnter(
        index: Int,
        durationMillis: Int = DURATION_STANDARD
    ): EnterTransition {
        val delay = calculateStaggerDelay(index)
        return fadeIn(
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delay.toInt(),
                easing = EASING_STANDARD
            )
        ) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delay.toInt(),
                easing = EASING_STANDARD
            )
        )
    }
    
    // ============================================
    // Shared Element Transition Helpers
    // ============================================
    
    /**
     * Standard duration for shared element transitions
     * Duration: 400ms for emphasis
     * 
     * Requirements: 11.4
     */
    const val SHARED_ELEMENT_DURATION = DURATION_SLOW
    
    /**
     * Creates animation spec for shared element transitions
     * Uses emphasized easing for smooth, noticeable transitions
     * 
     * Requirements: 11.4
     */
    fun <T> sharedElementAnimationSpec() = tween<T>(
        durationMillis = SHARED_ELEMENT_DURATION,
        easing = EASING_EMPHASIZED
    )
    
    // ============================================
    // Modifier Extensions for Easy Animation
    // ============================================
    
    /**
     * Applies fade-in animation to a composable using Modifier
     * Automatically triggers on composition
     * 
     * Requirements: 11.1
     * 
     * @param durationMillis Animation duration
     * @param delayMillis Optional delay before animation starts
     */
    @Composable
    fun Modifier.animatedFadeIn(
        durationMillis: Int = DURATION_STANDARD,
        delayMillis: Int = 0
    ): Modifier {
        var visible by remember { mutableStateOf(false) }
        
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = EASING_STANDARD
            ),
            label = "fade_in_alpha"
        )
        
        LaunchedEffect(Unit) {
            if (delayMillis > 0) {
                delay(delayMillis.toLong())
            }
            visible = true
        }
        
        return this.alpha(alpha)
    }
    
    /**
     * Applies scale animation to a composable using Modifier
     * Automatically triggers on composition
     * 
     * Requirements: 11.1
     * 
     * @param durationMillis Animation duration
     * @param delayMillis Optional delay before animation starts
     * @param initialScale Starting scale (default 0.95)
     */
    @Composable
    fun Modifier.animatedScale(
        durationMillis: Int = DURATION_STANDARD,
        delayMillis: Int = 0,
        initialScale: Float = 0.95f
    ): Modifier {
        var visible by remember { mutableStateOf(false) }
        
        val scale by animateFloatAsState(
            targetValue = if (visible) 1f else initialScale,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = EASING_STANDARD
            ),
            label = "scale_animation"
        )
        
        LaunchedEffect(Unit) {
            if (delayMillis > 0) {
                delay(delayMillis.toLong())
            }
            visible = true
        }
        
        return this.scale(scale)
    }
    
    /**
     * Applies combined fade-in and scale animation to a composable
     * This is the standard card animation
     * 
     * Requirements: 11.1
     * 
     * @param durationMillis Animation duration
     * @param delayMillis Optional delay before animation starts
     */
    @Composable
    fun Modifier.animatedCardAppearance(
        durationMillis: Int = DURATION_STANDARD,
        delayMillis: Int = 0
    ): Modifier {
        return this
            .animatedFadeIn(durationMillis, delayMillis)
            .animatedScale(durationMillis, delayMillis)
    }
    
    /**
     * Applies staggered animation to a list item
     * Combines fade-in and scale with calculated delay based on index
     * 
     * Requirements: 11.2
     * 
     * @param index The index of the item in the list
     * @param durationMillis Animation duration
     */
    @Composable
    fun Modifier.animatedStaggeredListItem(
        index: Int,
        durationMillis: Int = DURATION_STANDARD
    ): Modifier {
        val delay = calculateStaggerDelay(index).toInt()
        return this.animatedCardAppearance(durationMillis, delay)
    }
}
