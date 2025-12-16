package com.example.agrohub.data.repository

import com.example.agrohub.data.remote.api.LikeApiService
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
 * Integration tests for LikeRepository using MockWebServer.
 * Tests the full flow from repository through Retrofit to mock backend responses.
 * 
 * Validates: Requirements 13.1, 13.2, 13.3, 13.4, 13.5
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class LikeRepositoryIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var likeApiService: LikeApiService
    private lateinit var likeRepository: LikeRepositoryImpl
    
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
        
        likeApiService = retrofit.create(LikeApiService::class.java)
        
        // Create repository
        likeRepository = LikeRepositoryImpl(likeApiService)
    }
    
    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
    
    /**
     * Test liking a post successfully
     * Validates: Requirements 13.1
     */
    @Test
    fun `likePost success returns unit`() = runTest {
        // Given: Mock successful like response (204 No Content)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
        
        // When: Like a post
        val result = likeRepository.likePost(postId = 1L)
        
        // Then: Should return success
        assertTrue(result is Result.Success)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("likes/1") == true)
    }
    
    /**
     * Test unliking a post successfully
     * Validates: Requirements 13.2
     */
    @Test
    fun `unlikePost success returns unit`() = runTest {
        // Given: Mock successful unlike response (204 No Content)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
        
        // When: Unlike a post
        val result = likeRepository.unlikePost(postId = 1L)
        
        // Then: Should return success
        assertTrue(result is Result.Success)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertTrue(request.path?.contains("likes/1") == true)
    }
    
    /**
     * Test idempotent behavior of likePost - liking an already liked post
     * Validates: Requirements 13.1
     */
    @Test
    fun `likePost on already liked post is idempotent`() = runTest {
        // Given: Mock successful response (backend handles idempotency)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
        
        // When: Like an already liked post
        val result = likeRepository.likePost(postId = 1L)
        
        // Then: Should return success (idempotent operation)
        assertTrue(result is Result.Success)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("likes/1") == true)
    }
    
    /**
     * Test idempotent behavior of unlikePost - unliking an already unliked post
     * Validates: Requirements 13.2
     */
    @Test
    fun `unlikePost on already unliked post is idempotent`() = runTest {
        // Given: Mock successful response (backend handles idempotency)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
        
        // When: Unlike an already unliked post
        val result = likeRepository.unlikePost(postId = 1L)
        
        // Then: Should return success (idempotent operation)
        assertTrue(result is Result.Success)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertTrue(request.path?.contains("likes/1") == true)
    }
    
    /**
     * Test checking like status when post is liked
     * Validates: Requirements 13.3
     */
    @Test
    fun `checkLikeStatus returns true when post is liked`() = runTest {
        // Given: Mock response indicating post is liked
        val responseJson = """
            {
                "isLiked": true
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Check like status
        val result = likeRepository.checkLikeStatus(postId = 1L)
        
        // Then: Should return true
        assertTrue(result is Result.Success)
        val isLiked = (result as Result.Success).data
        assertTrue(isLiked)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("likes/1/check") == true)
    }
    
    /**
     * Test checking like status when post is not liked
     * Validates: Requirements 13.3
     */
    @Test
    fun `checkLikeStatus returns false when post is not liked`() = runTest {
        // Given: Mock response indicating post is not liked
        val responseJson = """
            {
                "isLiked": false
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Check like status
        val result = likeRepository.checkLikeStatus(postId = 1L)
        
        // Then: Should return false
        assertTrue(result is Result.Success)
        val isLiked = (result as Result.Success).data
        assertFalse(isLiked)
    }
    
    /**
     * Test getting like count for a post
     * Validates: Requirements 13.4
     */
    @Test
    fun `getLikeCount returns correct count`() = runTest {
        // Given: Mock response with like count
        val responseJson = """
            {
                "count": 42
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get like count
        val result = likeRepository.getLikeCount(postId = 1L)
        
        // Then: Should return correct count
        assertTrue(result is Result.Success)
        val count = (result as Result.Success).data
        assertEquals(42, count)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("likes/1/count") == true)
    }
    
    /**
     * Test getting like count for a post with zero likes
     * Validates: Requirements 13.4
     */
    @Test
    fun `getLikeCount returns zero for post with no likes`() = runTest {
        // Given: Mock response with zero count
        val responseJson = """
            {
                "count": 0
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get like count
        val result = likeRepository.getLikeCount(postId = 1L)
        
        // Then: Should return zero
        assertTrue(result is Result.Success)
        val count = (result as Result.Success).data
        assertEquals(0, count)
    }
    
    /**
     * Test getting batch like counts for multiple posts
     * Validates: Requirements 13.5
     */
    @Test
    fun `getBatchLikeCounts returns counts for multiple posts`() = runTest {
        // Given: Mock response with batch counts
        val responseJson = """
            {
                "counts": {
                    "1": 10,
                    "2": 25,
                    "3": 5
                }
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get batch like counts
        val result = likeRepository.getBatchLikeCounts(listOf(1L, 2L, 3L))
        
        // Then: Should return counts map
        assertTrue(result is Result.Success)
        val counts = (result as Result.Success).data
        assertEquals(3, counts.size)
        assertEquals(10, counts[1L])
        assertEquals(25, counts[2L])
        assertEquals(5, counts[3L])
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("likes/batch/counts") == true)
        
        // Verify request body contains post IDs
        val requestBody = request.body.readUtf8()
        assertTrue(requestBody.contains("postIds"))
    }
    
    /**
     * Test getting batch like counts with empty list
     * Validates: Requirements 13.5
     */
    @Test
    fun `getBatchLikeCounts with empty list returns empty map`() = runTest {
        // Given: Mock response with empty counts
        val responseJson = """
            {
                "counts": {}
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get batch like counts with empty list
        val result = likeRepository.getBatchLikeCounts(emptyList())
        
        // Then: Should return empty map
        assertTrue(result is Result.Success)
        val counts = (result as Result.Success).data
        assertTrue(counts.isEmpty())
    }
    
    /**
     * Test getting batch like counts with some posts having zero likes
     * Validates: Requirements 13.5
     */
    @Test
    fun `getBatchLikeCounts includes posts with zero likes`() = runTest {
        // Given: Mock response with mixed counts including zero
        val responseJson = """
            {
                "counts": {
                    "1": 10,
                    "2": 0,
                    "3": 5
                }
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get batch like counts
        val result = likeRepository.getBatchLikeCounts(listOf(1L, 2L, 3L))
        
        // Then: Should return all counts including zero
        assertTrue(result is Result.Success)
        val counts = (result as Result.Success).data
        assertEquals(3, counts.size)
        assertEquals(10, counts[1L])
        assertEquals(0, counts[2L])
        assertEquals(5, counts[3L])
    }
    
    /**
     * Test liking a non-existent post
     * Validates: Requirements 13.1
     */
    @Test
    fun `likePost with non-existent post returns not found error`() = runTest {
        // Given: Mock 404 Not Found response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 404,
                "error": "Not Found",
                "message": "Post not found",
                "path": "/api/likes/999"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Like non-existent post
        val result = likeRepository.likePost(postId = 999L)
        
        // Then: Should return not found error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.NotFoundException)
        assertEquals("Post not found", error.message)
    }
    
    /**
     * Test checking like status for non-existent post
     * Validates: Requirements 13.3
     */
    @Test
    fun `checkLikeStatus with non-existent post returns not found error`() = runTest {
        // Given: Mock 404 Not Found response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 404,
                "error": "Not Found",
                "message": "Post not found",
                "path": "/api/likes/999/check"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Check like status for non-existent post
        val result = likeRepository.checkLikeStatus(postId = 999L)
        
        // Then: Should return not found error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.NotFoundException)
        assertEquals("Post not found", error.message)
    }
    
    /**
     * Test authentication error when not logged in
     * Validates: Requirements 13.1
     */
    @Test
    fun `likePost without authentication returns authentication error`() = runTest {
        // Given: Mock 401 Unauthorized response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 401,
                "error": "Unauthorized",
                "message": "Authentication required",
                "path": "/api/likes/1"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Try to like without authentication
        val result = likeRepository.likePost(postId = 1L)
        
        // Then: Should return authentication error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.AuthenticationException)
        assertEquals("Authentication required", error.message)
    }
    
    /**
     * Test server error handling
     * Validates: Requirements 13.1
     */
    @Test
    fun `likePost with server error returns server exception`() = runTest {
        // Given: Mock 500 Internal Server Error response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )
        
        // When: Like a post when server has error
        val result = likeRepository.likePost(postId = 1L)
        
        // Then: Should return server error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ServerException)
        assertEquals("Server error. Please try again later.", error.message)
    }
}
