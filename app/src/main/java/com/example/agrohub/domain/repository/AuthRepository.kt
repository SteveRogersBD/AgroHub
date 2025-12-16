package com.example.agrohub.domain.repository

import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.util.Result

/**
 * Repository interface for authentication operations.
 * Handles user registration, login, token refresh, and logout.
 */
interface AuthRepository {
    /**
     * Registers a new user account.
     *
     * @param email User's email address
     * @param username User's unique username
     * @param password User's password
     * @return Result containing the created User on success, or an error
     */
    suspend fun register(email: String, username: String, password: String): Result<User>
    
    /**
     * Authenticates a user with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @return Result containing LoginResult with user and tokens on success, or an error
     */
    suspend fun login(email: String, password: String): Result<LoginResult>
    
    /**
     * Refreshes the access token using the stored refresh token.
     *
     * @return Result indicating success or failure of token refresh
     */
    suspend fun refreshToken(): Result<Unit>
    
    /**
     * Logs out the current user by clearing stored tokens.
     *
     * @return Result indicating success or failure of logout
     */
    suspend fun logout(): Result<Unit>
    
    /**
     * Checks if the user is currently authenticated with a valid token.
     *
     * @return true if authenticated with valid token, false otherwise
     */
    suspend fun isAuthenticated(): Boolean
}

/**
 * Data class containing the result of a successful login operation.
 *
 * @property user The authenticated user's information
 * @property accessToken The JWT access token for API requests
 * @property refreshToken The JWT refresh token for obtaining new access tokens
 */
data class LoginResult(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)
