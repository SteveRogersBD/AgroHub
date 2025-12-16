package com.example.agrohub.data.repository

import com.example.agrohub.data.remote.api.FeedApiService
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
 * Integration tests for FeedRepository using MockWebServer.
 * Tests the full flow from repository through Retrofit to mock backend responses.
 * 
 * Validates: Requirements 12.1, 12.2, 12.3, 12.4
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class FeedRepositoryIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var feedApiService: FeedApiService
    private lateinit var feedRepository: FeedRepositoryImpl
    
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
        
        feedApiService = retrofit.create(FeedApiService::class.java)
        
        // Create repository
        feedRepository = FeedRepositoryImpl(feedApiService)
    }
    
    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
    
    /**
     * Test feed retrieval with pagination
     * Validates: Requirements 12.1
     */
    @Test
    fun `getPersonalizedFeed with pagination returns feed posts`() = runTest {
        // Given: Mock successful feed response with pagination
        val responseJson = """
            {
                "content": [
                    {
                        "id": 1,
                        "userId": 100,
                        "username": "farmer_john",
                        "userAvatarUrl": "https://example.com/avatar1.jpg",
                        "content": "Great harvest this season!",
                        "mediaUrl": "https://example.com/harvest.jpg",
                        "likeCount": 15,
                        "commentCount": 3,
                        "likedByCurrentUser": true,
                        "createdAt": "2024-01-15T10:30:00.000Z",
                        "updatedAt": null
                    },
                    {
                        "id": 2,
                        "userId": 101,
                        "username": "agro_expert",
                        "userAvatarUrl": null,
                        "content": "Tips for organic farming",
                        "mediaUrl": null,
                        "likeCount": 8,
                        "commentCount": 5,
                        "likedByCurrentUser": false,
                        "createdAt": "2024-01-15T09:15:00.000Z",
                        "updatedAt": "2024-01-15T09:20:00.000Z"
                    }
                ],
                "pageable": {
                    "pageNumber": 0,
                    "pageSize": 10
                },
                "totalElements": 25,
                "totalPages": 3,
                "last": false
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get personalized feed
        val result = feedRepository.getPersonalizedFeed(page = 0, size = 10)
        
        // Then: Result should be successful with correct data
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        
        assertEquals(2, pagedData.items.size)
        assertEquals(0, pagedData.currentPage)
        assertEquals(10, pagedData.pageSize)
        assertEquals(25, pagedData.totalElements)
        assertEquals(3, pagedData.totalPages)
        assertFalse(pagedData.isLastPage)
        
        // Verify first post
        val firstPost = pagedData.items[0]
        assertEquals(1L, firstPost.id)
        assertEquals(100L, firstPost.author.id)
        assertEquals("farmer_john", firstPost.author.username)
        assertEquals("https://example.com/avatar1.jpg", firstPost.author.avatarUrl)
        assertEquals("Great harvest this season!", firstPost.content)
        assertEquals("https://example.com/harvest.jpg", firstPost.mediaUrl)
        assertEquals(15, firstPost.likeCount)
        assertEquals(3, firstPost.commentCount)
        assertTrue(firstPost.isLikedByCurrentUser)
        assertNotNull(firstPost.createdAt)
        assertNull(firstPost.updatedAt)
    }
    
    /**
     * Test feed caching behavior
     * Validates: Requirements 22.4
     */
    @Test
    fun `getPersonalizedFeed caches first page and getCachedFeed returns cached data`() = runTest {
        // Given: Mock successful feed response
        val responseJson = """
            {
                "content": [
                    {
                        "id": 1,
                        "userId": 100,
                        "username": "farmer_john",
                        "userAvatarUrl": null,
                        "content": "Cached post",
                        "mediaUrl": null,
                        "likeCount": 5,
                        "commentCount": 2,
                        "likedByCurrentUser": false,
                        "createdAt": "2024-01-15T10:30:00.000Z",
                        "updatedAt": null
                    }
                ],
                "pageable": {
                    "pageNumber": 0,
                    "pageSize": 10
                },
                "totalElements": 1,
                "totalPages": 1,
                "last": true
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get personalized feed (page 0)
        val result = feedRepository.getPersonalizedFeed(page = 0, size = 10)
        
        // Then: Result should be successful
        assertTrue(result is Result.Success)
        
        // And: Cached feed should contain the same data
        val cachedFeed = feedRepository.getCachedFeed()
        assertEquals(1, cachedFeed.size)
        assertEquals(1L, cachedFeed[0].id)
        assertEquals("Cached post", cachedFeed[0].content)
    }
    
    /**
     * Test that getCachedFeed returns empty list when no cache exists
     * Validates: Requirements 22.4
     */
    @Test
    fun `getCachedFeed returns empty list when no cache exists`() {
        // When: Get cached feed without making any API calls
        val cachedFeed = feedRepository.getCachedFeed()
        
        // Then: Should return empty list
        assertTrue(cachedFeed.isEmpty())
    }
    
    /**
     * Test enriched metadata (like counts, comment counts)
     * Validates: Requirements 12.2, 12.3
     */
    @Test
    fun `getPersonalizedFeed includes enriched metadata`() = runTest {
        // Given: Mock feed response with enriched metadata
        val responseJson = """
            {
                "content": [
                    {
                        "id": 1,
                        "userId": 100,
                        "username": "farmer_john",
                        "userAvatarUrl": "https://example.com/avatar.jpg",
                        "content": "Post with metadata",
                        "mediaUrl": null,
                        "likeCount": 42,
                        "commentCount": 17,
                        "likedByCurrentUser": true,
                        "createdAt": "2024-01-15T10:30:00.000Z",
                        "updatedAt": null
                    }
                ],
                "pageable": {
                    "pageNumber": 0,
                    "pageSize": 10
                },
                "totalElements": 1,
                "totalPages": 1,
                "last": true
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get personalized feed
        val result = feedRepository.getPersonalizedFeed(page = 0, size = 10)
        
        // Then: Result should include enriched metadata
        assertTrue(result is Result.Success)
        val post = (result as Result.Success).data.items[0]
        
        assertEquals(42, post.likeCount)
        assertEquals(17, post.commentCount)
    }
    
    /**
     * Test likedByCurrentUser flag
     * Validates: Requirements 12.3
     */
    @Test
    fun `getPersonalizedFeed correctly sets likedByCurrentUser flag`() = runTest {
        // Given: Mock feed response with mixed liked states
        val responseJson = """
            {
                "content": [
                    {
                        "id": 1,
                        "userId": 100,
                        "username": "user1",
                        "userAvatarUrl": null,
                        "content": "Liked post",
                        "mediaUrl": null,
                        "likeCount": 10,
                        "commentCount": 2,
                        "likedByCurrentUser": true,
                        "createdAt": "2024-01-15T10:30:00.000Z",
                        "updatedAt": null
                    },
                    {
                        "id": 2,
                        "userId": 101,
                        "username": "user2",
                        "userAvatarUrl": null,
                        "content": "Not liked post",
                        "mediaUrl": null,
                        "likeCount": 5,
                        "commentCount": 1,
                        "likedByCurrentUser": false,
                        "createdAt": "2024-01-15T09:15:00.000Z",
                        "updatedAt": null
                    }
                ],
                "pageable": {
                    "pageNumber": 0,
                    "pageSize": 10
                },
                "totalElements": 2,
                "totalPages": 1,
                "last": true
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get personalized feed
        val result = feedRepository.getPersonalizedFeed(page = 0, size = 10)
        
        // Then: likedByCurrentUser flags should be correct
        assertTrue(result is Result.Success)
        val posts = (result as Result.Success).data.items
        
        assertTrue(posts[0].isLikedByCurrentUser)
        assertFalse(posts[1].isLikedByCurrentUser)
    }
    
    /**
     * Test pagination with different pages
     * Validates: Requirements 12.1
     */
    @Test
    fun `getPersonalizedFeed with page 1 returns second page`() = runTest {
        // Given: Mock second page response
        val responseJson = """
            {
                "content": [
                    {
                        "id": 11,
                        "userId": 100,
                        "username": "user1",
                        "userAvatarUrl": null,
                        "content": "Post from page 2",
                        "mediaUrl": null,
                        "likeCount": 3,
                        "commentCount": 1,
                        "likedByCurrentUser": false,
                        "createdAt": "2024-01-14T10:30:00.000Z",
                        "updatedAt": null
                    }
                ],
                "pageable": {
                    "pageNumber": 1,
                    "pageSize": 10
                },
                "totalElements": 15,
                "totalPages": 2,
                "last": true
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get page 1
        val result = feedRepository.getPersonalizedFeed(page = 1, size = 10)
        
        // Then: Should return page 1 data
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        
        assertEquals(1, pagedData.currentPage)
        assertTrue(pagedData.isLastPage)
        assertEquals(11L, pagedData.items[0].id)
    }
    
    /**
     * Test error handling for 401 Unauthorized
     * Validates: Requirements 12.1
     */
    @Test
    fun `getPersonalizedFeed returns AuthenticationException on 401`() = runTest {
        // Given: Mock 401 response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\": \"Unauthorized\"}")
        )
        
        // When: Get personalized feed
        val result = feedRepository.getPersonalizedFeed(page = 0, size = 10)
        
        // Then: Should return AuthenticationException
        assertTrue(result is Result.Error)
        val exception = (result as Result.Error).exception
        assertTrue(exception is AppException.AuthenticationException)
    }
    
    /**
     * Test error handling for 500 Server Error
     * Validates: Requirements 12.1
     */
    @Test
    fun `getPersonalizedFeed returns ServerException on 500`() = runTest {
        // Given: Mock 500 response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": \"Internal Server Error\"}")
        )
        
        // When: Get personalized feed
        val result = feedRepository.getPersonalizedFeed(page = 0, size = 10)
        
        // Then: Should return ServerException
        assertTrue(result is Result.Error)
        val exception = (result as Result.Error).exception
        assertTrue(exception is AppException.ServerException)
    }
    
    /**
     * Test that only page 0 is cached
     * Validates: Requirements 22.4
     */
    @Test
    fun `getPersonalizedFeed only caches page 0`() = runTest {
        // Given: Mock response for page 1
        val responseJson = """
            {
                "content": [
                    {
                        "id": 10,
                        "userId": 100,
                        "username": "user1",
                        "userAvatarUrl": null,
                        "content": "Page 1 post",
                        "mediaUrl": null,
                        "likeCount": 1,
                        "commentCount": 0,
                        "likedByCurrentUser": false,
                        "createdAt": "2024-01-15T10:30:00.000Z",
                        "updatedAt": null
                    }
                ],
                "pageable": {
                    "pageNumber": 1,
                    "pageSize": 10
                },
                "totalElements": 15,
                "totalPages": 2,
                "last": false
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get page 1
        val result = feedRepository.getPersonalizedFeed(page = 1, size = 10)
        
        // Then: Result should be successful
        assertTrue(result is Result.Success)
        
        // And: Cache should still be empty (page 1 is not cached)
        val cachedFeed = feedRepository.getCachedFeed()
        assertTrue(cachedFeed.isEmpty())
    }
}
