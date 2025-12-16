package com.example.agrohub.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Basic unit tests for TokenManager implementation to verify core functionality.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class TokenManagerBasicTest {
    
    private lateinit var tokenManager: TokenManager
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        tokenManager = TokenManagerImpl(context)
    }
    
    @After
    fun teardown() = runTest {
        tokenManager.clearTokens()
    }
    
    @Test
    fun `saveTokens and getAccessToken returns correct token`() = runTest {
        val accessToken = "test_access_token_123"
        val refreshToken = "test_refresh_token_456"
        val expiresIn = 3600L
        
        tokenManager.saveTokens(accessToken, refreshToken, expiresIn)
        
        val retrieved = tokenManager.getAccessToken()
        assertEquals(accessToken, retrieved)
    }
    
    @Test
    fun `saveTokens and getRefreshToken returns correct token`() = runTest {
        val accessToken = "test_access_token_123"
        val refreshToken = "test_refresh_token_456"
        val expiresIn = 3600L
        
        tokenManager.saveTokens(accessToken, refreshToken, expiresIn)
        
        val retrieved = tokenManager.getRefreshToken()
        assertEquals(refreshToken, retrieved)
    }
    
    @Test
    fun `clearTokens removes all tokens`() = runTest {
        val accessToken = "test_access_token_123"
        val refreshToken = "test_refresh_token_456"
        val expiresIn = 3600L
        
        tokenManager.saveTokens(accessToken, refreshToken, expiresIn)
        tokenManager.clearTokens()
        
        assertNull(tokenManager.getAccessToken())
        assertNull(tokenManager.getRefreshToken())
        assertFalse(tokenManager.isAccessTokenValid())
    }
    
    @Test
    fun `isAccessTokenValid returns true for valid token`() = runTest {
        val accessToken = "test_access_token_123"
        val refreshToken = "test_refresh_token_456"
        val expiresIn = 3600L // 1 hour
        
        tokenManager.saveTokens(accessToken, refreshToken, expiresIn)
        
        assertTrue(tokenManager.isAccessTokenValid())
    }
    
    @Test
    fun `isAccessTokenValid returns false when no token exists`() = runTest {
        tokenManager.clearTokens()
        
        assertFalse(tokenManager.isAccessTokenValid())
    }
}
