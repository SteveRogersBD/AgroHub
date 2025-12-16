package com.example.agrohub.ui.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.agrohub.data.remote.NetworkModule
import com.example.agrohub.data.repository.WeatherRepository
import com.example.agrohub.models.ForecastDay
import com.example.agrohub.presentation.weather.WeatherState
import com.example.agrohub.presentation.weather.WeatherViewModel
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography
import com.example.agrohub.utils.LocationPermissionManager

/**
 * Weather screen with scrollable layout showing current weather, 7-day forecast, and alerts
 */
@Composable
fun WeatherScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = remember {
        val weatherApiService = NetworkModule.provideWeatherApiService()
        val repository = WeatherRepository(
            weatherApiService, 
            com.example.agrohub.BuildConfig.WEATHER_API_KEY
        )
        WeatherViewModel(repository)
    }
    
    val weatherState by viewModel.weatherState.collectAsState()
    
    // Get user location
    LaunchedEffect(Unit) {
        val locationManager = LocationPermissionManager(context)
        val location = locationManager.getUserLocation()
        val locationQuery = if (location != null) {
            "${location.first},${location.second}"
        } else {
            "kirksville" // Default location
        }
        viewModel.loadWeather(locationQuery)
    }
    
    when (val state = weatherState) {
        is WeatherState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AgroHubColors.DeepGreen)
            }
        }
        is WeatherState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error loading weather",
                        style = AgroHubTypography.Heading3,
                        color = AgroHubColors.CriticalRed
                    )
                    Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                    Text(
                        text = state.message,
                        style = AgroHubTypography.Body,
                        color = AgroHubColors.TextSecondary
                    )
                }
            }
        }
        is WeatherState.Success -> {
            val weather = state.weather
            
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(AgroHubColors.BackgroundLight),
                contentPadding = PaddingValues(AgroHubSpacing.md)
            ) {
                // Current Weather Section
                item {
                    weather.current?.let { current ->
                        CurrentWeatherSection(
                            location = weather.location?.name ?: "Unknown",
                            temperature = "${current.tempC?.toInt() ?: 0}°C",
                            humidity = "${current.humidity ?: 0}%",
                            condition = current.condition?.text ?: "N/A",
                            iconUrl = current.condition?.icon,
                            feelsLike = "${current.feelslikeC?.toInt() ?: 0}°C",
                            windSpeed = "${current.windKph?.toInt() ?: 0} km/h"
                        )
                    }
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
                    SevenDayForecast(
                        forecastDays = weather.forecast?.forecastday ?: emptyList(),
                        onDayClick = { forecastDay ->
                            // Navigate to detail screen with serialized data
                            val dayIndex = weather.forecast?.forecastday?.indexOf(forecastDay) ?: 0
                            navController.currentBackStackEntry?.savedStateHandle?.set("forecastDay", forecastDay)
                            navController.navigate("weather_detail/$dayIndex")
                        }
                    )
                    Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
                }
                
                // Weather Alerts Section
                weather.alerts?.alert?.let { alerts ->
                    if (alerts.isNotEmpty()) {
                        item {
                            Text(
                                text = "Weather Alerts",
                                style = AgroHubTypography.Heading2,
                                color = AgroHubColors.TextPrimary,
                                modifier = Modifier.padding(bottom = AgroHubSpacing.md)
                            )
                        }
                        
                        itemsIndexed(alerts) { index, alert ->
                            AlertCard(alert = alert as Map<String, String>)
                            if (index < alerts.size - 1) {
                                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
                            }
                        }
                    }
                }
                
                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(AgroHubSpacing.xl))
                }
            }
        }
    }
}

/**
 * Current weather section displaying temperature, humidity, and weather icon
 */
@Composable
fun CurrentWeatherSection(
    location: String,
    temperature: String,
    humidity: String,
    condition: String,
    iconUrl: String?,
    feelsLike: String,
    windSpeed: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
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
            Column {
                Text(
                    text = location,
                    style = AgroHubTypography.Heading2,
                    color = AgroHubColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = temperature,
                            style = AgroHubTypography.Heading1,
                            color = AgroHubColors.DeepGreen
                        )
                        Text(
                            text = condition,
                            style = AgroHubTypography.Body,
                            color = AgroHubColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                        Text(
                            text = "Feels like $feelsLike",
                            style = AgroHubTypography.Caption,
                            color = AgroHubColors.TextSecondary
                        )
                    }
                    
                    iconUrl?.let {
                        AsyncImage(
                            model = "https:$it",
                            contentDescription = condition,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherInfoItem(
                        icon = AgroHubIcons.Humidity,
                        label = "Humidity",
                        value = humidity
                    )
                    WeatherInfoItem(
                        icon = AgroHubIcons.Wind,
                        label = "Wind",
                        value = windSpeed
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherInfoItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AgroHubColors.SkyBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
        Text(
            text = label,
            style = AgroHubTypography.Caption,
            color = AgroHubColors.TextSecondary
        )
        Text(
            text = value,
            style = AgroHubTypography.Body,
            color = AgroHubColors.TextPrimary
        )
    }
}

/**
 * Seven-day forecast with horizontal scrolling
 */
@Composable
fun SevenDayForecast(
    forecastDays: List<ForecastDay>,
    onDayClick: (ForecastDay) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    // Debug: Log the number of forecast days
    LaunchedEffect(forecastDays.size) {
        println("Weather Debug: Received ${forecastDays.size} forecast days")
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        // Add padding at start
        Spacer(modifier = Modifier.width(4.dp))
        
        forecastDays.forEach { forecastDay ->
            ForecastCard(
                forecastDay = forecastDay,
                onClick = { onDayClick(forecastDay) }
            )
        }
        
        // Add padding at end
        Spacer(modifier = Modifier.width(4.dp))
    }
}

/**
 * Forecast card with gradient background showing daily weather
 */
@Composable
fun ForecastCard(
    forecastDay: ForecastDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AgroHubColors.SkyBlue.copy(alpha = 0.15f),
                            AgroHubColors.White
                        )
                    )
                )
                .padding(AgroHubSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Date
            Text(
                text = forecastDay.date?.substring(5) ?: "N/A",
                style = AgroHubTypography.Heading3,
                color = AgroHubColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Weather icon
            forecastDay.day?.condition?.icon?.let { iconUrl ->
                AsyncImage(
                    model = "https:$iconUrl",
                    contentDescription = forecastDay.day?.condition?.text,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Temperature high
            Text(
                text = "${forecastDay.day?.maxTempC?.toInt() ?: 0}°",
                style = AgroHubTypography.Heading3,
                color = AgroHubColors.DeepGreen
            )
            
            // Temperature low
            Text(
                text = "${forecastDay.day?.minTempC?.toInt() ?: 0}°",
                style = AgroHubTypography.Caption,
                color = AgroHubColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Condition
            Text(
                text = forecastDay.day?.condition?.text ?: "N/A",
                style = AgroHubTypography.Caption,
                color = AgroHubColors.TextSecondary,
                maxLines = 2
            )
        }
    }
}

/**
 * Alert card with severity color coding
 */
@Composable
fun AlertCard(
    alert: Map<String, String>,
    modifier: Modifier = Modifier
) {
    val headline = alert["headline"] ?: "Weather Alert"
    val severity = alert["severity"] ?: "Moderate"
    val description = alert["desc"] ?: "No description available"
    
    val severityColor = when (severity.lowercase()) {
        "extreme" -> AgroHubColors.CriticalRed
        "severe" -> AgroHubColors.CriticalRed
        "moderate" -> AgroHubColors.WarningYellow
        "minor" -> AgroHubColors.HealthyGreen
        else -> AgroHubColors.SkyBlue
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
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
                    imageVector = AgroHubIcons.Alert,
                    contentDescription = headline,
                    tint = severityColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Alert content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = headline,
                        style = AgroHubTypography.Heading3,
                        color = AgroHubColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = severityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = severity,
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
                    text = description,
                    style = AgroHubTypography.Body,
                    color = AgroHubColors.TextSecondary,
                    maxLines = 3
                )
            }
        }
    }
}
