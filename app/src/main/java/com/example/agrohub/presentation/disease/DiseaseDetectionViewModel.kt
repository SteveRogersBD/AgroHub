package com.example.agrohub.presentation.disease

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrohub.data.remote.NetworkModule
import com.example.agrohub.models.*
import com.example.agrohub.services.DiseaseDetectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for disease detection feature
 */
class DiseaseDetectionViewModel(private val context: Context) : ViewModel() {

    private val diseaseService = DiseaseDetectionService(context)
    private val serpApiService = NetworkModule.provideSerpApiService()

    private val _uiState = MutableStateFlow<DiseaseDetectionUiState>(DiseaseDetectionUiState.Initial)
    val uiState: StateFlow<DiseaseDetectionUiState> = _uiState.asStateFlow()

    private val _newsState = MutableStateFlow<NewsState>(NewsState.Loading)
    val newsState: StateFlow<NewsState> = _newsState.asStateFlow()

    /**
     * Analyze disease from input
     */
    fun analyzeDisease(input: DiseaseDetectionInput) {
        viewModelScope.launch {
            _uiState.value = DiseaseDetectionUiState.Loading
            try {
                val result = diseaseService.detectDisease(input)
                _uiState.value = DiseaseDetectionUiState.Success(result)
                
                // Load related news articles
                loadRelatedNews(result.diseaseName)
            } catch (e: Exception) {
                _uiState.value = DiseaseDetectionUiState.Error(
                    e.message ?: "Failed to analyze disease"
                )
            }
        }
    }

    /**
     * Load news articles related to the disease
     */
    private fun loadRelatedNews(diseaseName: String) {
        viewModelScope.launch {
            _newsState.value = NewsState.Loading
            try {
                val query = "$diseaseName crop disease treatment"
                val response = serpApiService.getNews(query = query)
                val articles = response.newsResults ?: emptyList()
                _newsState.value = NewsState.Success(articles)
            } catch (e: Exception) {
                _newsState.value = NewsState.Error(
                    e.message ?: "Failed to load news"
                )
            }
        }
    }

    /**
     * Reset to initial state
     */
    fun reset() {
        _uiState.value = DiseaseDetectionUiState.Initial
        _newsState.value = NewsState.Loading
    }
}

/**
 * UI state for disease detection
 */
sealed class DiseaseDetectionUiState {
    object Initial : DiseaseDetectionUiState()
    object Loading : DiseaseDetectionUiState()
    data class Success(val result: DiseaseDetectionResult) : DiseaseDetectionUiState()
    data class Error(val message: String) : DiseaseDetectionUiState()
}

/**
 * State for news articles
 */
sealed class NewsState {
    object Loading : NewsState()
    data class Success(val articles: List<NewsResult>) : NewsState()
    data class Error(val message: String) : NewsState()
}
