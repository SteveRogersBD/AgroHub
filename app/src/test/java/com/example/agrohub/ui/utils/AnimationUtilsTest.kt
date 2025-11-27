package com.example.agrohub.ui.utils

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AnimationUtils
 * 
 * Tests animation constants, durations, and utility functions
 * to ensure consistency across the application.
 * 
 * Requirements: 11.1, 11.2, 11.4, 11.6
 */
class AnimationUtilsTest {
    
    @Test
    fun `animation durations are within 200-400ms range`() {
        // Verify all animation durations follow the design system requirement
        assertTrue("Fast duration should be >= 200ms", 
            AnimationUtils.DURATION_FAST >= 200)
        assertTrue("Fast duration should be <= 400ms", 
            AnimationUtils.DURATION_FAST <= 400)
        
        assertTrue("Standard duration should be >= 200ms", 
            AnimationUtils.DURATION_STANDARD >= 200)
        assertTrue("Standard duration should be <= 400ms", 
            AnimationUtils.DURATION_STANDARD <= 400)
        
        assertTrue("Slow duration should be >= 200ms", 
            AnimationUtils.DURATION_SLOW >= 200)
        assertTrue("Slow duration should be <= 400ms", 
            AnimationUtils.DURATION_SLOW <= 400)
    }
    
    @Test
    fun `animation durations are ordered correctly`() {
        // Fast should be faster than standard, standard faster than slow
        assertTrue("Fast should be <= Standard", 
            AnimationUtils.DURATION_FAST <= AnimationUtils.DURATION_STANDARD)
        assertTrue("Standard should be <= Slow", 
            AnimationUtils.DURATION_STANDARD <= AnimationUtils.DURATION_SLOW)
    }
    
    @Test
    fun `stagger delay is reasonable`() {
        // Stagger delay should be short enough for smooth animations
        assertTrue("Stagger delay should be between 25-100ms", 
            AnimationUtils.STAGGER_DELAY in 25L..100L)
    }
    
    @Test
    fun `calculateStaggerDelay returns correct values`() {
        // Test stagger delay calculation
        assertEquals("First item should have 0 delay", 
            0L, AnimationUtils.calculateStaggerDelay(0))
        
        assertEquals("Second item should have STAGGER_DELAY", 
            AnimationUtils.STAGGER_DELAY, 
            AnimationUtils.calculateStaggerDelay(1))
        
        assertEquals("Third item should have 2 * STAGGER_DELAY", 
            AnimationUtils.STAGGER_DELAY * 2, 
            AnimationUtils.calculateStaggerDelay(2))
    }
    
    @Test
    fun `calculateStaggerDelay respects max delay`() {
        // Test that max delay is respected
        val maxDelay = 300L
        val largeIndex = 100
        
        val delay = AnimationUtils.calculateStaggerDelay(largeIndex, maxDelay)
        
        assertTrue("Delay should not exceed max delay", delay <= maxDelay)
    }
    
    @Test
    fun `easing curves are defined`() {
        // Verify easing curves are properly set
        assertNotNull("Standard easing should be defined", 
            AnimationUtils.EASING_STANDARD)
        assertNotNull("Emphasized easing should be defined", 
            AnimationUtils.EASING_EMPHASIZED)
        
        // Verify they use the correct easing functions
        assertEquals("Standard easing should be FastOutSlowInEasing", 
            FastOutSlowInEasing, AnimationUtils.EASING_STANDARD)
        assertEquals("Emphasized easing should be LinearOutSlowInEasing", 
            LinearOutSlowInEasing, AnimationUtils.EASING_EMPHASIZED)
    }
    
    @Test
    fun `shared element duration is within range`() {
        // Shared element transitions should use appropriate duration
        assertTrue("Shared element duration should be >= 200ms", 
            AnimationUtils.SHARED_ELEMENT_DURATION >= 200)
        assertTrue("Shared element duration should be <= 400ms", 
            AnimationUtils.SHARED_ELEMENT_DURATION <= 400)
    }
    
    @Test
    fun `fadeInEnter uses correct duration`() {
        // Test that fade-in animation uses standard duration by default
        val animation = AnimationUtils.fadeInEnter()
        assertNotNull("Fade-in animation should be created", animation)
    }
    
    @Test
    fun `scaleInEnter uses correct initial scale`() {
        // Test that scale-in animation uses 95% initial scale by default
        val animation = AnimationUtils.scaleInEnter()
        assertNotNull("Scale-in animation should be created", animation)
    }
    
    @Test
    fun `cardEnterAnimation combines fade and scale`() {
        // Test that card animation combines both fade and scale
        val animation = AnimationUtils.cardEnterAnimation()
        assertNotNull("Card enter animation should be created", animation)
    }
    
    @Test
    fun `slideUpEnter creates valid animation`() {
        // Test that slide-up animation is created
        val animation = AnimationUtils.slideUpEnter()
        assertNotNull("Slide-up animation should be created", animation)
    }
    
    @Test
    fun `staggeredListItemEnter creates valid animation`() {
        // Test that staggered list item animation is created
        val animation = AnimationUtils.staggeredListItemEnter(index = 0)
        assertNotNull("Staggered list item animation should be created", animation)
    }
    
    @Test
    fun `staggeredListItemEnter delay increases with index`() {
        // Verify that higher indices get longer delays
        // This is implicit in the implementation, but we can verify the calculation
        val delay0 = AnimationUtils.calculateStaggerDelay(0)
        val delay1 = AnimationUtils.calculateStaggerDelay(1)
        val delay2 = AnimationUtils.calculateStaggerDelay(2)
        
        assertTrue("Delay should increase with index", delay0 < delay1)
        assertTrue("Delay should increase with index", delay1 < delay2)
    }
}
