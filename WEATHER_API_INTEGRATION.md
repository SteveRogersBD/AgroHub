# Weather API Integration Guide

## Overview
The weather feature has been successfully integrated with the WeatherAPI.com service to display real-time weather data in the AgroHub app.

## Features Implemented

### 1. Weather API Service
- **Location**: `app/src/main/java/com/example/agrohub/data/remote/api/WeatherApiService.kt`
- Retrofit service interface for WeatherAPI.com
- Fetches 7-day forecast with air quality and alerts
- Base URL: `https://api.weatherapi.com/v1/`

### 2. Weather Repository
- **Location**: `app/src/main/java/com/example/agrohub/data/repository/WeatherRepository.kt`
- Handles weather data operations
- Provides clean API for fetching forecasts
- Error handling with Result type

### 3. Weather ViewModel
- **Location**: `app/src/main/java/com/example/agrohub/presentation/weather/WeatherViewModel.kt`
- Manages weather state (Loading, Success, Error)
- Uses Kotlin Coroutines for async operations
- Exposes StateFlow for UI observation

### 4. Weather Screen (Updated)
- **Location**: `app/src/main/java/com/example/agrohub/ui/screens/weather/WeatherScreen.kt`
- Displays current weather with location, temperature, humidity, and wind
- Shows 7-day forecast in horizontal scrollable cards
- Displays weather alerts if available
- Automatically fetches user location or defaults to "kirksville"
- Each forecast card is clickable to view detailed information

### 5. Weather Detail Screen (New)
- **Location**: `app/src/main/java/com/example/agrohub/ui/screens/weather/WeatherDetailScreen.kt`
- Shows detailed weather information for a selected day
- Displays hourly forecast for the entire day
- Shows astronomy data (sunrise, sunset, moon phase)
- Includes comprehensive weather metrics (humidity, wind, precipitation, UV index, etc.)

## UI Components

### Current Weather Card
- Location name
- Current temperature and condition
- Weather icon from API
- "Feels like" temperature
- Humidity and wind speed

### 7-Day Forecast Cards
- Horizontal scrollable view
- Each card shows:
  - Date (MM-DD format)
  - Weather icon
  - High/Low temperatures
  - Weather condition text
- Cards are clickable to navigate to detail screen

### Weather Detail Screen
- Day summary with full weather information
- Hourly forecast cards showing:
  - Time
  - Weather icon
  - Temperature
  - Condition
  - Humidity
- Astronomy information (sunrise, sunset, moon phase)

## API Configuration

### API Key
The Weather API key is configured in `local.properties`:
```properties
WEATHER_API_KEY=68b568e462ee44f683d12500251612
```

The key is injected into BuildConfig during build time and accessed via:
```kotlin
BuildConfig.WEATHER_API_KEY
```

### API Endpoint
```
GET https://api.weatherapi.com/v1/forecast.json
Parameters:
- key: API key
- q: Location (lat,lon or city name)
- days: 7
- aqi: yes (air quality index)
- alerts: yes (weather alerts)
```

## Data Models

### WeatherResponse
All data classes in `WeatherResponse.kt` have been updated to implement `Parcelable` for navigation:
- WeatherResponse
- Location
- Current
- Forecast
- ForecastDay
- Day
- Hour
- Astro
- Condition
- AirQuality
- Alerts

## Navigation

### Routes
1. **Weather Screen**: `Routes.WEATHER` (bottom navigation)
2. **Weather Detail**: `weather_detail/{dayIndex}` (navigated from forecast card click)

### Navigation Flow
1. User clicks Weather icon in bottom navigation
2. Weather screen loads and fetches data based on user location
3. User clicks on any of the 7 forecast cards
4. App navigates to Weather Detail screen with selected day's data
5. User can view hourly forecast and detailed metrics
6. Back button returns to Weather screen

## Location Integration

The weather screen automatically:
1. Checks for saved user location using `LocationPermissionManager`
2. Uses lat/lon coordinates if available
3. Falls back to "kirksville" as default location
4. Fetches weather data for the determined location

## Error Handling

The app handles three states:
1. **Loading**: Shows circular progress indicator
2. **Success**: Displays weather data
3. **Error**: Shows error message with details

## Dependencies Added

### Parcelize Plugin
Added to `app/build.gradle.kts`:
```kotlin
id("kotlin-parcelize")
```

### Network Module Update
Added `provideWeatherApiService()` method to create a separate Retrofit instance for Weather API (different base URL from backend services).

## Testing

To test the integration:
1. Launch the app
2. Click on the Weather icon in the bottom navigation
3. Verify current weather displays correctly
4. Scroll through the 7-day forecast cards
5. Click on any forecast card
6. Verify detailed weather information displays
7. Check hourly forecast scrolls properly
8. Use back button to return to main weather screen

## Future Enhancements

Potential improvements:
1. Add pull-to-refresh functionality
2. Cache weather data to reduce API calls
3. Add weather notifications for severe alerts
4. Support multiple saved locations
5. Add weather-based farming recommendations
6. Integrate with crop planning features
