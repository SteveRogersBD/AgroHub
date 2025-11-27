package com.example.agrohub.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.agrohub.models.RiskLevel
import com.example.agrohub.ui.components.cards.GradientCard
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * WeatherWarningCard - Displays current weather with risk level color coding
 * 
 * Shows:
 * - Temperature
 * - Weather condition
 * - Risk level indicator with color coding
 * 
 * Color coding:
 * - Green for LOW risk
 * - Yellow for MEDIUM risk
 * - Red for HIGH risk
 * 
 * Requirements: 2.4
 * 
 * @param temperature Current temperature
 * @param condition Weather condition description
 * @param riskLevel Risk level for farming activities
 */
@Composable
fun WeatherWarningCard(
    temperature: String,
    condition: String,
    riskLevel: RiskLevel
) {
    // Determine colors based on risk level
    val (backgroundColor, textColor, riskText) = when (riskLevel) {
        RiskLevel.LOW -> Triple(
            AgroHubColors.HealthyGreen,
            AgroHubColors.White,
            "Low Risk"
        )
        RiskLevel.MEDIUM -> Triple(
            AgroHubColors.WarningYellow,
            AgroHubColors.CharcoalText,
            "Medium Risk"
        )
        RiskLevel.HIGH -> Triple(
            AgroHubColors.CriticalRed,
            AgroHubColors.White,
            "High Risk"
        )
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        // Section Title
        Text(
            text = "Today's Weather",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
        
        // Weather Card
        GradientCard(
            gradient = Brush.linearGradient(
                colors = listOf(
                    backgroundColor,
                    backgroundColor.copy(alpha = 0.8f)
                )
            ),
            elevation = 6.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Weather Info
                Column(
                    verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.xs)
                ) {
                    Text(
                        text = temperature,
                        style = AgroHubTypography.Heading1,
                        color = textColor
                    )
                    Text(
                        text = condition,
                        style = AgroHubTypography.Body,
                        color = textColor
                    )
                    Text(
                        text = riskText,
                        style = AgroHubTypography.Body,
                        color = textColor
                    )
                }
                
                // Weather Icon
                Icon(
                    imageVector = AgroHubIcons.Weather,
                    contentDescription = "$condition weather with $riskText risk level",
                    tint = textColor,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}
