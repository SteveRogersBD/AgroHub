package com.example.agrohub.data.remote.interceptor

import com.example.agrohub.security.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that automatically injects JWT authentication tokens into API requests.
 * 
 * This interceptor:
 * - Reads the access token from TokenManager
 * - Adds Authorization: Bearer {token} header to authenticated requests
 * - Skips authentication for public endpoints (/auth/register, /auth/login)
 * 
 * Requirements: 4.1, 4.2, 4.5
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    companion object {
        private val UNAUTHENTICATED_ENDPOINTS = setOf(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh"
        )
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        
        // Skip auth for public endpoints
        if (UNAUTHENTICATED_ENDPOINTS.any { path.contains(it) }) {
            return chain.proceed(originalRequest)
        }
        
        // Get access token
        val accessToken = runBlocking {
            tokenManager.getAccessToken()
        }
        
        // If no token available, proceed without auth header
        if (accessToken == null) {
            return chain.proceed(originalRequest)
        }
        
        // Add Authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
}
