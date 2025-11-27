package com.example.agrohub.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * AgroHub Light Color Scheme
 */
private val LightColorScheme = lightColorScheme(
    primary = AgroHubColors.DeepGreen,
    onPrimary = AgroHubColors.White,
    primaryContainer = AgroHubColors.LightGreen,
    onPrimaryContainer = AgroHubColors.DarkGreen,
    
    secondary = AgroHubColors.GoldenHarvest,
    onSecondary = AgroHubColors.CharcoalText,
    secondaryContainer = AgroHubColors.GoldenHarvest.copy(alpha = 0.2f),
    onSecondaryContainer = AgroHubColors.CharcoalText,
    
    tertiary = AgroHubColors.SkyBlue,
    onTertiary = AgroHubColors.White,
    tertiaryContainer = AgroHubColors.SkyBlue.copy(alpha = 0.2f),
    onTertiaryContainer = AgroHubColors.CharcoalText,
    
    background = AgroHubColors.BackgroundLight,
    onBackground = AgroHubColors.TextPrimary,
    
    surface = AgroHubColors.BackgroundWhite,
    onSurface = AgroHubColors.TextPrimary,
    surfaceVariant = AgroHubColors.SurfaceLight,
    onSurfaceVariant = AgroHubColors.TextSecondary,
    
    error = AgroHubColors.CriticalRed,
    onError = AgroHubColors.White,
    
    outline = AgroHubColors.TextHint,
    outlineVariant = AgroHubColors.SurfaceLight
)

/**
 * AgroHub Dark Color Scheme
 */
private val DarkColorScheme = darkColorScheme(
    primary = AgroHubColors.MediumGreen,
    onPrimary = AgroHubColors.DarkGreen,
    primaryContainer = AgroHubColors.DarkGreen,
    onPrimaryContainer = AgroHubColors.LightGreen,
    
    secondary = AgroHubColors.GoldenHarvest,
    onSecondary = AgroHubColors.CharcoalText,
    secondaryContainer = AgroHubColors.GoldenHarvest.copy(alpha = 0.3f),
    onSecondaryContainer = AgroHubColors.GoldenHarvest,
    
    tertiary = AgroHubColors.SkyBlue,
    onTertiary = AgroHubColors.CharcoalText,
    tertiaryContainer = AgroHubColors.SkyBlue.copy(alpha = 0.3f),
    onTertiaryContainer = AgroHubColors.SkyBlue,
    
    background = AgroHubColors.CharcoalText,
    onBackground = AgroHubColors.White,
    
    surface = AgroHubColors.CharcoalText.copy(alpha = 0.95f),
    onSurface = AgroHubColors.White,
    surfaceVariant = AgroHubColors.CharcoalText.copy(alpha = 0.8f),
    onSurfaceVariant = AgroHubColors.TextHint,
    
    error = AgroHubColors.CriticalRed,
    onError = AgroHubColors.White,
    
    outline = AgroHubColors.TextSecondary,
    outlineVariant = AgroHubColors.CharcoalText.copy(alpha = 0.6f)
)

/**
 * AgroHub Theme Composable
 * 
 * Main theme provider for the AgroHub application.
 * Applies the design system including colors, typography, shapes, and spacing.
 * 
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param content The composable content to be themed
 */
@Composable
fun AgroHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AgroHubTypography.Default,
        shapes = AgroHubShapes.Default,
        content = content
    )
}
