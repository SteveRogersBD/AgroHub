package com.example.agrohub.ui.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.agrohub.models.ForecastDay
import com.example.agrohub.models.Hour
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography
import coil.compose.AsyncImage

/**
 * Detailed weather screen for a specific day
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailScreen(
    navController: NavController,
    forecastDay: ForecastDay?,
    modifier: Modifier = Modifier
) {
    if (forecastDay == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No data available", style = AgroHubTypography.Body)
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(forecastDay.date ?: "Weather Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgroHubColors.DeepGreen,
                    titleContentColor = AgroHubColors.White,
                    navigationIconContentColor = AgroHubColors.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(AgroHubColors.BackgroundLight)
                .padding(padding)
                .padding(AgroHubSpacing.md)
        ) {
            // Day Summary
            item {
                DaySummaryCard(forecastDay)
                Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
            }
            
            // Hourly Forecast
            item {
                Text(
                    text = "Hourly Forecast",
                    style = AgroHubTypography.Heading2,
                    color = AgroHubColors.TextPrimary,
                    modifier = Modifier.padding(bottom = AgroHubSpacing.md)
                )
            }
            
            items(forecastDay.hour ?: emptyList()) { hour ->
                HourlyForecastCard(hour)
                Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            }
        }
    }
}

@Composable
fun DaySummaryCard(forecastDay: ForecastDay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = forecastDay.day?.condition?.text ?: "N/A",
                        style = AgroHubTypography.Heading2,
                        color = AgroHubColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                    Text(
                        text = "${forecastDay.day?.maxTempC?.toInt() ?: 0}°C / ${forecastDay.day?.minTempC?.toInt() ?: 0}°C",
                        style = AgroHubTypography.Heading3,
                        color = AgroHubColors.DeepGreen
                    )
                }
                
                forecastDay.day?.condition?.icon?.let { iconUrl ->
                    AsyncImage(
                        model = "https:$iconUrl",
                        contentDescription = "Weather icon",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
            
            // Weather details grid
            Column(verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)) {
                WeatherDetailRow("Humidity", "${forecastDay.day?.avgHumidity ?: 0}%")
                WeatherDetailRow("Wind", "${forecastDay.day?.maxWindKph?.toInt() ?: 0} km/h")
                WeatherDetailRow("Precipitation", "${forecastDay.day?.totalPrecipMm ?: 0} mm")
                WeatherDetailRow("UV Index", "${forecastDay.day?.uv?.toInt() ?: 0}")
                WeatherDetailRow("Chance of Rain", "${forecastDay.day?.dailyChanceOfRain ?: 0}%")
                
                forecastDay.astro?.let { astro ->
                    Divider(modifier = Modifier.padding(vertical = AgroHubSpacing.sm))
                    Text(
                        text = "Astronomy",
                        style = AgroHubTypography.Heading3,
                        color = AgroHubColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                    WeatherDetailRow("Sunrise", astro.sunrise ?: "N/A")
                    WeatherDetailRow("Sunset", astro.sunset ?: "N/A")
                    WeatherDetailRow("Moon Phase", astro.moonPhase ?: "N/A")
                }
            }
        }
    }
}

@Composable
fun WeatherDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = AgroHubTypography.Body,
            color = AgroHubColors.TextSecondary
        )
        Text(
            text = value,
            style = AgroHubTypography.Body,
            color = AgroHubColors.TextPrimary
        )
    }
}

@Composable
fun HourlyForecastCard(hour: Hour) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Text(
                text = hour.time?.substring(11) ?: "N/A",
                style = AgroHubTypography.Body,
                color = AgroHubColors.TextPrimary,
                modifier = Modifier.width(60.dp)
            )
            
            // Icon
            hour.condition?.icon?.let { iconUrl ->
                AsyncImage(
                    model = "https:$iconUrl",
                    contentDescription = "Weather icon",
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Temperature
            Text(
                text = "${hour.tempC?.toInt() ?: 0}°C",
                style = AgroHubTypography.Heading3,
                color = AgroHubColors.DeepGreen,
                modifier = Modifier.width(60.dp)
            )
            
            // Condition
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hour.condition?.text ?: "N/A",
                    style = AgroHubTypography.Caption,
                    color = AgroHubColors.TextSecondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
                ) {
                    Icon(
                        imageVector = AgroHubIcons.Humidity,
                        contentDescription = "Humidity",
                        tint = AgroHubColors.SkyBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${hour.humidity ?: 0}%",
                        style = AgroHubTypography.Caption,
                        color = AgroHubColors.TextSecondary
                    )
                }
            }
        }
    }
}
