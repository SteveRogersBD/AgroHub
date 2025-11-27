package com.example.agrohub.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * AgroHub Color Palette
 * Primary color palette for the AgroHub application
 */
object AgroHubColors {
    // Primary Colors
    val DeepGreen = Color(0xFF2A7B3F)
    val DarkGreen = Color(0xFF1F4E2E)
    val White = Color(0xFFFFFFFF)
    val GoldenHarvest = Color(0xFFF2C94C)
    val SkyBlue = Color(0xFF56CCF2)
    val CharcoalText = Color(0xFF1A1A1A)
    
    // Extended Colors
    val LightGreen = Color(0xFFE8F5E9)
    val MediumGreen = Color(0xFF66BB6A)
    
    // Status Colors
    val HealthyGreen = Color(0xFF4CAF50)
    val WarningYellow = Color(0xFFFFC107)
    val CriticalRed = Color(0xFFF44336)
    
    // Background Colors
    val BackgroundLight = Color(0xFFFAFAFA)
    val BackgroundWhite = Color(0xFFFFFFFF)
    val SurfaceLight = Color(0xFFF5F5F5)
    
    // Text Colors
    val TextPrimary = CharcoalText
    val TextSecondary = Color(0xFF757575)
    val TextHint = Color(0xFFBDBDBD)
    
    // All colors in the palette for validation
    val palette = setOf(
        DeepGreen, DarkGreen, White, GoldenHarvest, SkyBlue, CharcoalText,
        LightGreen, MediumGreen, HealthyGreen, WarningYellow, CriticalRed,
        BackgroundLight, BackgroundWhite, SurfaceLight,
        TextPrimary, TextSecondary, TextHint
    )
}
