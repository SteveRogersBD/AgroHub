package com.example.agrohub.domain.util

/**
 * Sealed class hierarchy representing different types of application exceptions.
 * Each exception type provides user-friendly error messages.
 */
sealed class AppException(override val message: String) : Exception(message) {
    
    /**
     * Represents network-related errors (timeouts, no connection, etc.)
     */
    data class NetworkException(
        override val message: String = "Network error occurred. Please check your connection."
    ) : AppException(message)
    
    /**
     * Represents authentication failures (invalid credentials, etc.)
     */
    data class AuthenticationException(
        override val message: String = "Authentication failed. Please check your credentials."
    ) : AppException(message)
    
    /**
     * Represents authorization failures (insufficient permissions, etc.)
     */
    data class AuthorizationException(
        override val message: String = "Access denied. You don't have permission to perform this action."
    ) : AppException(message)
    
    /**
     * Represents resource not found errors (404)
     */
    data class NotFoundException(
        override val message: String = "The requested resource was not found."
    ) : AppException(message)
    
    /**
     * Represents validation errors (invalid input, duplicate resources, etc.)
     */
    data class ValidationException(
        override val message: String = "Validation failed. Please check your input."
    ) : AppException(message)
    
    /**
     * Represents server errors (5xx responses)
     */
    data class ServerException(
        override val message: String = "Server error occurred. Please try again later."
    ) : AppException(message)
    
    /**
     * Represents unknown or unexpected errors
     */
    data class UnknownException(
        override val message: String = "An unknown error occurred. Please try again."
    ) : AppException(message)
}
