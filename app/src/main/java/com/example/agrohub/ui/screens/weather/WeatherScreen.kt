package com.example.agrohub.ui.screens.weather

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.agrohub.data.MockDataProvider
import com.example.agrohub.models.DailyForecast
import com.example.agrohub.models.RiskLevel
import com.example.agrohub.models.WeatherAlert
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * Weather screen with scrollable layout showing current weather, 7-day forecast, and alerts
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5
 */
@Composable
fun WeatherScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val forecastData = remember { MockDataProvider.generateWeatherForecast() }
    val alertsData = remember { MockDataProvider.generateWeatherAlerts() }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AgroHubColors.BackgroundLight),
        contentPadding = PaddingValues(AgroHubSpacing.md)
    ) {
        // Current Weather Section
        item {
            CurrentWeatherSection(
                temperature = "28°C",
                humidity = "65%",
                condition = "Partly Cloudy",
                icon = AgroHubIcons.Weather
            )
            Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
        }
        
        // 7-Day Forecast Section
        item {
            Text(
                text = "7-Day Forecast",
                style = AgroHubTypography.Heading2,
                color = AgroHubColors.TextPrimary,
                modifier = Modifier.padding(bottom = AgroHubSpacing.md)
            )
            SevenDayForecast(forecastData = forecastData)
            Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
        }
        
        // Weather Alerts Section
        item {
            Text(
                text = "Weather Alerts",
                style = AgroHubTypography.Heading2,
                color = AgroHubColors.TextPrimary,
                modifier = Modifier.padding(bottom = AgroHubSpacing.md)
            )
        }
        
        itemsIndexed(alertsData) { index, alert ->
            val animationDelay = index * 50
            var visible by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(animationDelay.toLong())
                visible = true
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(300)) + 
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(300)
                        )
            ) {
                AlertCard(alert = alert)
            }
            
            if (index < alertsData.size - 1) {
                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            }
        }
        
        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(AgroHubSpacing.xl))
        }
    }
}

/**
 * Current weather section displaying temperature, humidity, and weather icon
 * 
 * Requirements: 4.1
 */
@Composable
fun CurrentWeatherSection(
    temperature: String,
    humidity: String,
    condition: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) + 
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(400)
                )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = AgroHubColors.White
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                AgroHubColors.DeepGreen.copy(alpha = 0.1f),
                                AgroHubColors.SkyBlue.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .padding(AgroHubSpacing.lg)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Current Weather",
                            style = AgroHubTypography.Heading3,
                            color = AgroHubColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                        Text(
                            text = temperature,
                            style = AgroHubTypography.Heading1,
                            color = AgroHubColors.DeepGreen
                        )
                        Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
                        Text(
                            text = condition,
                            style = AgroHubTypography.Body,
                            color = AgroHubColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(AgroHubSpacing.md))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = AgroHubIcons.Humidity,
                                contentDescription = "Humidity",
                                tint = AgroHubColors.SkyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(AgroHubSpacing.xs))
                            Text(
                                text = "Humidity: $humidity",
                                style = AgroHubTypography.Body,
                                color = AgroHubColors.TextSecondary
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = condition,
                        tint = AgroHubColors.SkyBlue,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }
    }
}

/**
 * Seven-day forecast with horizontal scrolling
 * 
 * Requirements: 4.2
 */
@Composable
fun SevenDayForecast(
    forecastData: List<DailyForecast>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        forecastData.forEachIndexed { index, forecast ->
            val animationDelay = index * 80
            var visible by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(animationDelay.toLong())
                visible = true
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(300)) + 
                        slideInHorizontally(
                            initialOffsetX = { it / 2 },
                            animationSpec = tween(300)
                        )
            ) {
                ForecastCard(forecast = forecast)
            }
        }
    }
}

/**
 * Forecast card with gradient background showing daily weather
 * 
 * Requirements: 4.2, 4.3
 */
@Composable
fun ForecastCard(
    forecast: DailyForecast,
    modifier: Modifier = Modifier
) {
    val riskColor = when (forecast.riskLevel) {
        RiskLevel.LOW -> AgroHubColors.HealthyGreen
        RiskLevel.MEDIUM -> AgroHubColors.WarningYellow
        RiskLevel.HIGH -> AgroHubColors.CriticalRed
    }
    
    Card(
        modifier = modifier
            .width(120.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = AgroHubColors.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            riskColor.copy(alpha = 0.15f),
                            AgroHubColors.White
                        )
                    )
                )
                .padding(AgroHubSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = forecast.date,
                style = AgroHubTypography.Heading3,
                color = AgroHubColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            Icon(
                imageVector = forecast.icon,
                contentDescription = forecast.condition,
                tint = AgroHubColors.SkyBlue,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            Text(
                text = forecast.tempHigh,
                style = AgroHubTypography.Heading3,
                color = AgroHubColors.DeepGreen
            )
            
            Text(
                text = forecast.tempLow,
                style = AgroHubTypography.Caption,
                color = AgroHubColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Risk level indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(riskColor)
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
            
            Text(
                text = forecast.riskLevel.name,
                style = AgroHubTypography.Caption,
                color = riskColor
            )
        }
    }
}

/**
 * Weather alerts list showing alert cards
 * 
 * Requirements: 4.4
 */
@Composable
fun WeatherAlertsList(
    alerts: List<WeatherAlert>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        alerts.forEach { alert ->
            AlertCard(alert = alert)
        }
    }
}

/**
 * Alert card with severity color coding
 * 
 * Requirements: 4.4, 4.5
 */
@Composable
fun AlertCard(
    alert: WeatherAlert,
    modifier: Modifier = Modifier
) {
    val severityColor = when (alert.severity.uppercase()) {
        "HIGH" -> AgroHubColors.CriticalRed
        "MEDIUM" -> AgroHubColors.WarningYellow
        "LOW" -> AgroHubColors.HealthyGreen
        else -> AgroHubColors.SkyBlue
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = AgroHubColors.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
        ) {
            // Severity indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(severityColor)
            )
            
            // Alert icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(severityColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = alert.icon,
                    contentDescription = alert.alertType,
                    tint = severityColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Alert content
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = alert.alertType,
                        style = AgroHubTypography.Heading3,
                        color = AgroHubColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = severityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = alert.severity,
                            style = AgroHubTypography.Caption,
                            color = severityColor,
                            modifier = Modifier.padding(
                                horizontal = AgroHubSpacing.sm,
                                vertical = AgroHubSpacing.xs
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
                
                Text(
                    text = alert.description,
                    style = AgroHubTypography.Body,
                    color = AgroHubColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                
                Text(
                    text = alert.timestamp,
                    style = AgroHubTypography.Caption,
                    color = AgroHubColors.TextHint
                )
            }
        }
    }
}
