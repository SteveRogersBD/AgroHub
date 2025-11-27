package com.example.agrohub.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * AgroHub Typography System
 * Defines consistent text styles across the application
 */
object AgroHubTypography {
    val Heading1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = AgroHubColors.TextPrimary
    )
    
    val Heading2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = AgroHubColors.TextPrimary
    )
    
    val Heading3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = AgroHubColors.TextPrimary
    )
    
    val Body = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = AgroHubColors.TextPrimary
    )
    
    val Caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = AgroHubColors.TextSecondary
    )
    
    // Material3 Typography mapping
    val Default = Typography(
        displayLarge = Heading1,
        displayMedium = Heading2,
        displaySmall = Heading3,
        headlineLarge = Heading1,
        headlineMedium = Heading2,
        headlineSmall = Heading3,
        titleLarge = Heading2,
        titleMedium = Heading3,
        titleSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
            color = AgroHubColors.TextPrimary
        ),
        bodyLarge = Body,
        bodyMedium = Body,
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
            color = AgroHubColors.TextPrimary
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
            color = AgroHubColors.TextPrimary
        ),
        labelMedium = Caption,
        labelSmall = Caption
    )
}
