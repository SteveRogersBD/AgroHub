package com.example.agrohub.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrohub.data.repository.WeatherRepository
import com.example.agrohub.models.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for weather screen
 */
class WeatherViewModel(
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()
    
    fun loadWeather(location: String) {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            
            weatherRepository.getForecast(location).fold(
                onSuccess = { response ->
                    _weatherState.value = WeatherState.Success(response)
                },
                onFailure = { error ->
                    _weatherState.value = WeatherState.Error(
                        error.message ?: "Failed to load weather data"
                    )
                }
            )
        }
    }
}

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val weather: WeatherResponse) : WeatherState()
    data class Error(val message: String) : WeatherState()
}
