package com.example.agrohub.domain.util

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utility object for mapping exceptions to AppException types.
 * 
 * This centralizes error mapping logic to ensure consistent error handling
 * across all repositories and provides user-friendly error messages.
 */
object ErrorMapper {
    
    /**
     * Maps any exception to an appropriate AppException type.
     * 
     * @param e The exception to map
     * @return AppException with appropriate type and user-friendly message
     */
    fun mapException(e: Exception): AppException {
        return when (e) {
            is HttpException -> mapHttpException(e)
            is IOException -> mapNetworkException(e)
            is AppException -> e // Already an AppException, return as-is
            else -> AppException.UnknownException(e.message ?: "An unknown error occurred")
        }
    }
    
    /**
     * Maps HTTP exceptions based on status code to appropriate AppException types.
     * 
     * Handles:
     * - 400: ValidationException with error details
     * - 401: AuthenticationException
     * - 403: AuthorizationException
     * - 404: NotFoundException
     * - 409: ValidationException (duplicate resource)
     * - 5xx: ServerException
     * - Other: UnknownException
     *
     * @param e The HttpException to map
     * @return AppException with appropriate type and message
     */
    fun mapHttpException(e: HttpException): AppException {
        return when (e.code()) {
            400 -> {
                // Try to parse error body for more specific validation message
                val errorMessage = try {
                    e.response()?.errorBody()?.string()?.let { body ->
                        // Extract message from error body if available
                        // This is a simple extraction; could be enhanced with JSON parsing
                        if (body.contains("message")) {
                            body
                        } else {
                            "Invalid request. Please check your input."
                        }
                    } ?: "Invalid request. Please check your input."
                } catch (ex: Exception) {
                    "Invalid request. Please check your input."
                }
                AppException.ValidationException(errorMessage)
            }
            401 -> AppException.AuthenticationException(
                "Authentication required. Please log in again."
            )
            403 -> AppException.AuthorizationException(
                "Access denied. You don't have permission to perform this action."
            )
            404 -> AppException.NotFoundException(
                "The requested resource was not found."
            )
            409 -> AppException.ValidationException(
                "Resource already exists. Please use a different value."
            )
            in 500..599 -> AppException.ServerException(
                "Server error occurred. Please try again later."
            )
            else -> AppException.UnknownException(
                "An unexpected error occurred (HTTP ${e.code()})"
            )
        }
    }
    
    /**
     * Maps network/IO exceptions to NetworkException with specific messages.
     * 
     * Handles:
     * - SocketTimeoutException: Request timeout
     * - UnknownHostException: Unable to reach server
     * - Other IOException: General network error
     *
     * @param e The IOException to map
     * @return NetworkException with appropriate message
     */
    fun mapNetworkException(e: IOException): AppException {
        return when (e) {
            is SocketTimeoutException -> AppException.NetworkException(
                "Request timed out. Please check your connection and try again."
            )
            is UnknownHostException -> AppException.NetworkException(
                "Unable to reach server. Please check your internet connection."
            )
            else -> AppException.NetworkException(
                "Network error occurred. Please check your connection."
            )
        }
    }
}
