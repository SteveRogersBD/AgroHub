package com.example.agrohub.security

/**
 * Interface for managing secure storage and retrieval of authentication tokens.
 * Implementations should use encrypted storage mechanisms to protect sensitive token data.
 */
interface TokenManager {
    /**
     * Saves authentication tokens securely.
     *
     * @param accessToken The JWT access token
     * @param refreshToken The JWT refresh token
     * @param expiresIn The token expiration time in seconds
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long)
    
    /**
     * Retrieves the stored access token.
     *
     * @return The access token, or null if not available
     */
    suspend fun getAccessToken(): String?
    
    /**
     * Retrieves the stored refresh token.
     *
     * @return The refresh token, or null if not available
     */
    suspend fun getRefreshToken(): String?
    
    /**
     * Checks if the stored access token is still valid (not expired).
     *
     * @return true if a valid access token exists, false otherwise
     */
    suspend fun isAccessTokenValid(): Boolean
    
    /**
     * Clears all stored tokens from secure storage.
     * Should be called on logout.
     */
    suspend fun clearTokens()
}
