package com.example.agrohub.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException

/**
 * Interceptor that logs HTTP request and response details.
 * 
 * This interceptor:
 * - Logs request method, URL, headers, and body
 * - Logs response status, headers, and body
 * - Logs execution time
 * - Should only be enabled in debug builds
 * 
 * Requirements: 1.2, 1.5
 */
class LoggingInterceptor(
    private val isDebug: Boolean
) : Interceptor {
    
    companion object {
        private const val TAG = "NetworkLog"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isDebug) {
            return chain.proceed(chain.request())
        }
        
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        
        // Log request
        logRequest(request)
        
        // Execute request
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            Log.e(TAG, "Request failed: ${e.message}", e)
            throw e
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Log response
        logResponse(response, duration)
        
        return response
    }
    
    private fun logRequest(request: okhttp3.Request) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "→ ${request.method} ${request.url}")
        
        // Log headers
        val headers = request.headers
        if (headers.size > 0) {
            Log.d(TAG, "Headers:")
            for (i in 0 until headers.size) {
                val name = headers.name(i)
                val value = if (name.equals("Authorization", ignoreCase = true)) {
                    "Bearer ***" // Mask token for security
                } else {
                    headers.value(i)
                }
                Log.d(TAG, "  $name: $value")
            }
        }
        
        // Log body
        val requestBody = request.body
        if (requestBody != null) {
            try {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                val bodyString = buffer.readUtf8()
                Log.d(TAG, "Body: $bodyString")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to read request body", e)
            }
        }
    }
    
    private fun logResponse(response: Response, duration: Long) {
        Log.d(TAG, "← ${response.code} ${response.message} (${duration}ms)")
        
        // Log headers
        val headers = response.headers
        if (headers.size > 0) {
            Log.d(TAG, "Headers:")
            for (i in 0 until headers.size) {
                Log.d(TAG, "  ${headers.name(i)}: ${headers.value(i)}")
            }
        }
        
        // Log body (peek without consuming)
        val responseBody = response.body
        if (responseBody != null) {
            try {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE) // Buffer the entire body
                val buffer = source.buffer
                val bodyString = buffer.clone().readUtf8()
                
                // Truncate if too long
                val truncatedBody = if (bodyString.length > 1000) {
                    "${bodyString.substring(0, 1000)}... (truncated)"
                } else {
                    bodyString
                }
                Log.d(TAG, "Body: $truncatedBody")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to read response body", e)
            }
        }
        
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}
