package com.example.agrohub.data.repository

import android.util.Patterns
import com.example.agrohub.data.mapper.DateTimeUtils
import com.example.agrohub.data.mapper.UserMapper
import com.example.agrohub.data.remote.api.AuthApiService
import com.example.agrohub.data.remote.dto.LoginRequestDto
import com.example.agrohub.data.remote.dto.RefreshTokenRequestDto
import com.example.agrohub.data.remote.dto.RegisterRequestDto
import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.repository.AuthRepository
import com.example.agrohub.domain.repository.LoginResult
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import com.example.agrohub.security.TokenManager
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDateTime

/**
 * Implementation of AuthRepository that handles authentication operations.
 * 
 * This repository manages user registration, login, token refresh, and logout
 * by communicating with the backend API and managing secure token storage.
 *
 * @property authApiService The Retrofit service for authentication endpoints
 * @property tokenManager The manager for secure token storage and retrieval
 */
class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {
    
    override suspend fun register(
        email: String,
        username: String,
        password: String
    ): Result<User> {
        return try {
            // Validate email format before making API call
            if (!isValidEmail(email)) {
                return Result.Error(
                    AppException.ValidationException("Invalid email format")
                )
            }
            
            val request = RegisterRequestDto(
                email = email,
                username = username,
                password = password
            )
            
            val response = authApiService.register(request)
            
            // Backend returns LoginResponse with tokens for registration
            // Store tokens securely
            tokenManager.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                expiresIn = response.expiresIn
            )
            
            // Map response to User domain model
            val user = User(
                id = response.userId,
                email = response.email,
                username = response.username,
                name = "",
                bio = "",
                avatarUrl = null,
                location = "",
                website = null,
                createdAt = LocalDateTime.now(), // Backend doesn't return this in registration response
                updatedAt = null
            )
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun login(email: String, password: String): Result<LoginResult> {
        return try {
            // Validate email format before making API call
            if (!isValidEmail(email)) {
                return Result.Error(
                    AppException.ValidationException("Invalid email format")
                )
            }
            
            val request = LoginRequestDto(
                emailOrUsername = email,
                password = password
            )
            
            val response = authApiService.login(request)
            
            // Store tokens securely
            tokenManager.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                expiresIn = response.expiresIn
            )
            
            // Map response to User domain model
            val user = User(
                id = response.userId,
                email = response.email,
                username = response.username,
                name = "",
                bio = "",
                avatarUrl = null,
                location = "",
                website = null,
                createdAt = LocalDateTime.now(), // Backend doesn't return this in login response
                updatedAt = null
            )
            
            val loginResult = LoginResult(
                user = user,
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )
            
            Result.Success(loginResult)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun refreshToken(): Result<Unit> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
                ?: return Result.Error(
                    AppException.AuthenticationException("No refresh token available")
                )
            
            val request = RefreshTokenRequestDto(refreshToken = refreshToken)
            val response = authApiService.refreshToken(request)
            
            // Update stored tokens with new access token
            tokenManager.saveTokens(
                accessToken = response.accessToken,
                refreshToken = refreshToken, // Keep the same refresh token
                expiresIn = response.expiresIn
            )
            
            Result.Success(Unit)
        } catch (e: Exception) {
            // If refresh fails, clear tokens
            tokenManager.clearTokens()
            Result.Error(mapException(e))
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            tokenManager.clearTokens()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(
                AppException.UnknownException("Failed to logout: ${e.message}")
            )
        }
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return tokenManager.isAccessTokenValid()
    }
    
    /**
     * Validates email format using Android's Patterns utility.
     *
     * @param email The email string to validate
     * @return true if email format is valid, false otherwise
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Maps exceptions to appropriate AppException types with user-friendly messages.
     *
     * @param e The exception to map
     * @return AppException with appropriate type and message
     */
    private fun mapException(e: Exception): AppException {
        return when (e) {
            is HttpException -> mapHttpException(e)
            is IOException -> mapNetworkException(e)
            else -> AppException.UnknownException(e.message ?: "An unknown error occurred")
        }
    }
    
    /**
     * Maps HTTP exceptions based on status code.
     *
     * @param e The HttpException to map
     * @return AppException with appropriate type and message
     */
    private fun mapHttpException(e: HttpException): AppException {
        return when (e.code()) {
            400 -> {
                // Try to parse error body for more specific message
                val errorMessage = try {
                    e.response()?.errorBody()?.string() ?: "Invalid request"
                } catch (ex: Exception) {
                    "Invalid request"
                }
                AppException.ValidationException(errorMessage)
            }
            401 -> AppException.AuthenticationException("Invalid credentials")
            403 -> AppException.AuthorizationException("Access denied")
            404 -> AppException.NotFoundException("Resource not found")
            409 -> AppException.ValidationException("Email or username already exists")
            in 500..599 -> AppException.ServerException("Server error. Please try again later.")
            else -> AppException.UnknownException("An unexpected error occurred")
        }
    }
    
    /**
     * Maps network/IO exceptions to NetworkException.
     *
     * @param e The IOException to map
     * @return NetworkException with appropriate message
     */
    private fun mapNetworkException(e: IOException): AppException {
        return when (e) {
            is SocketTimeoutException -> 
                AppException.NetworkException("Request timed out. Please check your connection.")
            is UnknownHostException -> 
                AppException.NetworkException("Unable to reach server. Please check your internet connection.")
            else -> 
                AppException.NetworkException("Network error occurred. Please check your connection.")
        }
    }
}
