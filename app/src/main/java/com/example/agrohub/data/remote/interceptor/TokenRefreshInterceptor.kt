package com.example.agrohub.data.remote.interceptor

import com.example.agrohub.security.TokenManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Interceptor that handles token expiration and automatic refresh.
 * 
 * This interceptor:
 * - Intercepts 401 Unauthorized responses
 * - Attempts to refresh the access token using the refresh token
 * - Retries the original request with the new token
 * - Synchronizes multiple concurrent refresh attempts using a mutex
 * - Clears tokens and fails if refresh is unsuccessful
 * 
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
 */
class TokenRefreshInterceptor(
    private val tokenManager: TokenManager,
    private val refreshTokenCall: suspend (String) -> Pair<String, Long>? // Returns (accessToken, expiresIn) or null
) : Interceptor {
    
    private val refreshMutex = Mutex()
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)
        
        // If not 401, return response as-is
        if (response.code != 401) {
            return response
        }
        
        // Close the original response body
        response.close()
        
        // Attempt token refresh
        val refreshSuccessful = runBlocking {
            refreshMutex.withLock {
                attemptTokenRefresh()
            }
        }
        
        if (!refreshSuccessful) {
            // Refresh failed, clear tokens and return 401
            runBlocking {
                tokenManager.clearTokens()
            }
            return response
        }
        
        // Retry original request with new token
        val newAccessToken = runBlocking {
            tokenManager.getAccessToken()
        }
        
        if (newAccessToken == null) {
            return response
        }
        
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
        
        return chain.proceed(newRequest)
    }
    
    /**
     * Attempts to refresh the access token.
     * Returns true if successful, false otherwise.
     */
    private suspend fun attemptTokenRefresh(): Boolean {
        val refreshToken = tokenManager.getRefreshToken() ?: return false
        
        return try {
            val result = refreshTokenCall(refreshToken)
            if (result != null) {
                val (newAccessToken, expiresIn) = result
                tokenManager.saveTokens(newAccessToken, refreshToken, expiresIn)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
