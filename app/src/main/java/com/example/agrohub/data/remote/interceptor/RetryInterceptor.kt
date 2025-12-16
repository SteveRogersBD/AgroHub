package com.example.agrohub.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Interceptor that implements retry logic for transient failures.
 * 
 * This interceptor:
 * - Retries on network timeouts (max 3 attempts)
 * - Retries on 5xx server errors with exponential backoff
 * - Does not retry 4xx client errors
 * - Configurable retry delay and max attempts
 * 
 * Requirements: 23.1, 23.2, 23.3
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMillis: Long = 1000
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var attempt = 0
        
        while (attempt < maxRetries) {
            try {
                // Clear previous response if exists
                response?.close()
                
                // Execute request
                response = chain.proceed(request)
                
                // Check if we should retry based on status code
                if (shouldRetry(response, attempt)) {
                    val delay = calculateDelay(attempt)
                    Thread.sleep(delay)
                    attempt++
                    continue
                }
                
                // Success or non-retryable error
                return response
                
            } catch (e: SocketTimeoutException) {
                exception = e
                // Retry on timeout
                if (attempt < maxRetries - 1) {
                    val delay = calculateDelay(attempt)
                    Thread.sleep(delay)
                    attempt++
                } else {
                    throw e
                }
            } catch (e: IOException) {
                exception = e
                // Don't retry other IOExceptions
                throw e
            }
        }
        
        // If we exhausted retries, throw the last exception or return last response
        if (exception != null) {
            throw exception
        }
        
        return response ?: throw IOException("No response after $maxRetries attempts")
    }
    
    /**
     * Determines if a response should be retried.
     * - 5xx errors: retry with exponential backoff
     * - 4xx errors: do not retry
     * - 2xx/3xx: do not retry (success)
     */
    private fun shouldRetry(response: Response, attempt: Int): Boolean {
        val code = response.code
        
        // Don't retry if we've exhausted attempts
        if (attempt >= maxRetries - 1) {
            return false
        }
        
        // Retry 5xx server errors
        if (code in 500..599) {
            return true
        }
        
        // Don't retry 4xx client errors or successful responses
        return false
    }
    
    /**
     * Calculates exponential backoff delay.
     * Delay = initialDelay * 2^attempt
     */
    private fun calculateDelay(attempt: Int): Long {
        return initialDelayMillis * (1 shl attempt) // 2^attempt
    }
}
