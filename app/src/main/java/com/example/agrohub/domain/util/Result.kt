package com.example.agrohub.domain.util

/**
 * A sealed class representing the result of an operation.
 * Used throughout the application to handle success, error, and loading states.
 *
 * @param T The type of data returned on success
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with data
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * Represents a failed operation with an exception
     */
    data class Error(val exception: AppException) : Result<Nothing>()
    
    /**
     * Represents an operation in progress
     */
    data object Loading : Result<Nothing>()
}
