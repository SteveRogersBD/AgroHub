package com.example.agrohub.data.repository

import com.example.agrohub.data.remote.api.AuthApiService
import com.example.agrohub.domain.repository.LoginResult
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import com.example.agrohub.security.TokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Integration tests for AuthRepository using MockWebServer.
 * Tests the full flow from repository through Retrofit to mock backend responses.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class AuthRepositoryIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var authApiService: AuthApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepositoryImpl
    
    @Before
    fun setup() {
        // Setup MockWebServer
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        // Setup Moshi
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        
        // Setup OkHttp client with short timeouts for tests
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()
        
        // Setup Retrofit with mock server URL
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        
        authApiService = retrofit.create(AuthApiService::class.java)
        
        // Mock TokenManager
        tokenManager = mockk(relaxed = true)
        
        // Create repository
        authRepository = AuthRepositoryImpl(authApiService, tokenManager)
    }
    
    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
    
    /**
     * Test successful registration flow
     * Validates: Requirements 6.1
     */
    @Test
    fun `register success returns user`() = runTest {
        // Given: Mock successful registration response
        val responseJson = """
            {
                "id": 123,
                "email": "test@example.com",
                "username": "testuser",
                "role": "USER",
                "createdAt": "2024-01-15T10:30:00.000Z"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Register a new user
        val result = authRepository.register(
            email = "test@example.com",
            username = "testuser",
            password = "password123"
        )
        
        // Then: Should return success with user data
        assertTrue(result is Result.Success)
        val user = (result as Result.Success).data
        assertEquals(123L, user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("testuser", user.username)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("auth/register") == true)
    }
    
    /**
     * Test successful login flow with token storage
     * Validates: Requirements 6.2, 7.1, 7.3
     */
    @Test
    fun `login success returns user and stores tokens`() = runTest {
        // Given: Mock successful login response
        val responseJson = """
            {
                "accessToken": "access_token_123",
                "refreshToken": "refresh_token_456",
                "tokenType": "Bearer",
                "expiresIn": 3600,
                "userId": 123,
                "username": "testuser",
                "email": "test@example.com"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Login with credentials
        val result = authRepository.login(
            email = "test@example.com",
            password = "password123"
        )
        
        // Then: Should return success with login result
        assertTrue(result is Result.Success)
        val loginResult = (result as Result.Success).data
        assertEquals(123L, loginResult.user.id)
        assertEquals("testuser", loginResult.user.username)
        assertEquals("test@example.com", loginResult.user.email)
        assertEquals("access_token_123", loginResult.accessToken)
        assertEquals("refresh_token_456", loginResult.refreshToken)
        
        // Verify tokens were stored
        coVerify {
            tokenManager.saveTokens(
                accessToken = "access_token_123",
                refreshToken = "refresh_token_456",
                expiresIn = 3600
            )
        }
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("auth/login") == true)
    }
    
    /**
     * Test login with invalid credentials (401)
     * Validates: Requirements 7.4
     */
    @Test
    fun `login with invalid credentials returns authentication error`() = runTest {
        // Given: Mock 401 Unauthorized response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 401,
                "error": "Unauthorized",
                "message": "Invalid credentials",
                "path": "/api/auth/login"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Login with invalid credentials
        val result = authRepository.login(
            email = "test@example.com",
            password = "wrongpassword"
        )
        
        // Then: Should return authentication error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.AuthenticationException)
        assertEquals("Invalid credentials", error.message)
        
        // Verify tokens were not stored
        coVerify(exactly = 0) {
            tokenManager.saveTokens(any(), any(), any())
        }
    }
    
    /**
     * Test registration with duplicate email (409)
     * Validates: Requirements 6.3
     */
    @Test
    fun `register with duplicate email returns validation error`() = runTest {
        // Given: Mock 409 Conflict response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 409,
                "error": "Conflict",
                "message": "Email already exists",
                "path": "/api/auth/register"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(409)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Register with duplicate email
        val result = authRepository.register(
            email = "existing@example.com",
            username = "newuser",
            password = "password123"
        )
        
        // Then: Should return validation error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ValidationException)
        assertTrue(error.message.contains("already exists"))
    }
    
    /**
     * Test token refresh flow
     * Validates: Requirements 7.1, 7.3
     */
    @Test
    fun `refresh token success updates stored tokens`() = runTest {
        // Given: Mock refresh token in storage
        coEvery { tokenManager.getRefreshToken() } returns "refresh_token_456"
        
        // Mock successful refresh response
        val responseJson = """
            {
                "accessToken": "new_access_token_789",
                "tokenType": "Bearer",
                "expiresIn": 3600
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Refresh token
        val result = authRepository.refreshToken()
        
        // Then: Should return success
        assertTrue(result is Result.Success)
        
        // Verify new tokens were stored
        coVerify {
            tokenManager.saveTokens(
                accessToken = "new_access_token_789",
                refreshToken = "refresh_token_456", // Same refresh token
                expiresIn = 3600
            )
        }
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("auth/refresh") == true)
    }
    
    /**
     * Test logout clears tokens
     * Validates: Requirements 6.4
     */
    @Test
    fun `logout clears stored tokens`() = runTest {
        // When: Logout
        val result = authRepository.logout()
        
        // Then: Should return success
        assertTrue(result is Result.Success)
        
        // Verify tokens were cleared
        coVerify {
            tokenManager.clearTokens()
        }
    }
    
    /**
     * Test email validation before API call
     * Validates: Requirements 6.5
     */
    @Test
    fun `register with invalid email format fails without API call`() = runTest {
        // When: Register with invalid email
        val result = authRepository.register(
            email = "invalid-email",
            username = "testuser",
            password = "password123"
        )
        
        // Then: Should return validation error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ValidationException)
        assertEquals("Invalid email format", error.message)
        
        // Verify no API request was made
        assertEquals(0, mockWebServer.requestCount)
    }
    
    /**
     * Test login with invalid email format fails without API call
     * Validates: Requirements 6.5
     */
    @Test
    fun `login with invalid email format fails without API call`() = runTest {
        // When: Login with invalid email
        val result = authRepository.login(
            email = "@invalid",
            password = "password123"
        )
        
        // Then: Should return validation error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ValidationException)
        assertEquals("Invalid email format", error.message)
        
        // Verify no API request was made
        assertEquals(0, mockWebServer.requestCount)
    }
    
    /**
     * Test refresh token failure clears tokens
     * Validates: Requirements 7.3
     */
    @Test
    fun `refresh token failure clears stored tokens`() = runTest {
        // Given: Mock refresh token in storage
        coEvery { tokenManager.getRefreshToken() } returns "invalid_refresh_token"
        
        // Mock 401 Unauthorized response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("{}")
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Refresh token fails
        val result = authRepository.refreshToken()
        
        // Then: Should return error
        assertTrue(result is Result.Error)
        
        // Verify tokens were cleared
        coVerify {
            tokenManager.clearTokens()
        }
    }
    
    /**
     * Test isAuthenticated checks token validity
     * Validates: Requirements 3.1, 3.2
     */
    @Test
    fun `isAuthenticated returns token validity status`() = runTest {
        // Given: Mock token manager returns valid token
        coEvery { tokenManager.isAccessTokenValid() } returns true
        
        // When: Check authentication
        val isAuthenticated = authRepository.isAuthenticated()
        
        // Then: Should return true
        assertTrue(isAuthenticated)
        
        // Given: Mock token manager returns invalid token
        coEvery { tokenManager.isAccessTokenValid() } returns false
        
        // When: Check authentication again
        val isNotAuthenticated = authRepository.isAuthenticated()
        
        // Then: Should return false
        assertFalse(isNotAuthenticated)
    }
}
