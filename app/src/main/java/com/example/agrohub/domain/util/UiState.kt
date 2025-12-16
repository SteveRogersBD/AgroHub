package com.example.agrohub.domain.util

/**
 * A sealed class representing the UI state for ViewModels.
 * Provides a consistent way to handle loading, success, and error states in the UI layer.
 *
 * @param T The type of data to be displayed in the UI
 */
sealed class UiState<out T> {
    /**
     * Represents the initial idle state before any operation
     */
    data object Idle : UiState<Nothing>()
    
    /**
     * Represents a loading state while an operation is in progress
     */
    data object Loading : UiState<Nothing>()
    
    /**
     * Represents a successful state with data to display
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Represents an error state with an error message
     */
    data class Error(val message: String) : UiState<Nothing>()
}
