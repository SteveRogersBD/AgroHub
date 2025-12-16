package com.example.agrohub.data.repository

import com.example.agrohub.data.remote.api.FollowApiService
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
 * Integration tests for FollowRepository using MockWebServer.
 * Tests the full flow from repository through Retrofit to mock backend responses.
 * 
 * Validates: Requirements 10.1, 10.2, 10.3, 10.4
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class FollowRepositoryIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var followApiService: FollowApiService
    private lateinit var followRepository: FollowRepositoryImpl
    
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
        
        followApiService = retrofit.create(FollowApiService::class.java)
        
        // Create repository
        followRepository = FollowRepositoryImpl(followApiService)
    }
    
    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
    
    /**
     * Test follow user operation
     * Validates: Requirements 10.1
     */
    @Test
    fun `followUser success returns success result`() = runTest {
        // Given: Mock successful follow response (204 No Content)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
        
        // When: Follow a user
        val result = followRepository.followUser(userId = 123L)
        
        // Then: Should return success
        assertTrue(result is Result.Success)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("follows/123") == true)
    }
    
    /**
     * Test unfollow user operation
     * Validates: Requirements 10.2
     */
    @Test
    fun `unfollowUser success returns success result`() = runTest {
        // Given: Mock successful unfollow response (204 No Content)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
        
        // When: Unfollow a user
        val result = followRepository.unfollowUser(userId = 123L)
        
        // Then: Should return success
        assertTrue(result is Result.Success)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertTrue(request.path?.contains("follows/123") == true)
    }
    
    /**
     * Test idempotent follow behavior
     * Validates: Requirements 10.1
     */
    @Test
    fun `followUser when already following returns success (idempotent)`() = runTest {
        // Given: Mock successful response even if already following
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
        
        // When: Follow a user that is already followed
        val result = followRepository.followUser(userId = 456L)
        
        // Then: Should return success (idempotent operation)
        assertTrue(result is Result.Success)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("follows/456") == true)
    }
    
    /**
     * Test idempotent unfollow behavior
     * Validates: Requirements 10.2
     */
    @Test
    fun `unfollowUser when not following returns success (idempotent)`() = runTest {
        // Given: Mock successful response even if not following
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
        
        // When: Unfollow a user that is not followed
        val result = followRepository.unfollowUser(userId = 789L)
        
        // Then: Should return success (idempotent operation)
        assertTrue(result is Result.Success)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertTrue(request.path?.contains("follows/789") == true)
    }
    
    /**
     * Test check follow status - following
     * Validates: Requirements 10.3
     */
    @Test
    fun `checkFollowStatus returns true when following`() = runTest {
        // Given: Mock response indicating user is following
        val responseJson = """
            {
                "isFollowing": true
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Check follow status
        val result = followRepository.checkFollowStatus(userId = 123L)
        
        // Then: Should return true
        assertTrue(result is Result.Success)
        val isFollowing = (result as Result.Success).data
        assertTrue(isFollowing)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("follows/check/123") == true)
    }
    
    /**
     * Test check follow status - not following
     * Validates: Requirements 10.3
     */
    @Test
    fun `checkFollowStatus returns false when not following`() = runTest {
        // Given: Mock response indicating user is not following
        val responseJson = """
            {
                "isFollowing": false
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Check follow status
        val result = followRepository.checkFollowStatus(userId = 456L)
        
        // Then: Should return false
        assertTrue(result is Result.Success)
        val isFollowing = (result as Result.Success).data
        assertFalse(isFollowing)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("follows/check/456") == true)
    }
    
    /**
     * Test get follow stats
     * Validates: Requirements 10.4
     */
    @Test
    fun `getFollowStats returns correct statistics`() = runTest {
        // Given: Mock follow stats response
        val responseJson = """
            {
                "followersCount": 150,
                "followingCount": 75
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get follow stats
        val result = followRepository.getFollowStats(userId = 123L)
        
        // Then: Should return correct stats
        assertTrue(result is Result.Success)
        val stats = (result as Result.Success).data
        assertEquals(150, stats.followersCount)
        assertEquals(75, stats.followingCount)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("follows/123/stats") == true)
    }
    
    /**
     * Test get followers with pagination
     * Validates: Requirements 10.4
     */
    @Test
    fun `getFollowers returns paginated list of followers`() = runTest {
        // Given: Mock followers response
        val responseJson = """
            {
                "content": [
                    {
                        "id": 1,
                        "email": "follower1@example.com",
                        "username": "follower1",
                        "name": "Follower One",
                        "bio": "Bio 1",
                        "avatarUrl": null,
                        "location": "City 1",
                        "website": null,
                        "createdAt": "2024-01-15T10:30:00.000Z",
                        "updatedAt": null
                    },
                    {
                        "id": 2,
                        "email": "follower2@example.com",
                        "username": "follower2",
                        "name": "Follower Two",
                        "bio": "Bio 2",
                        "avatarUrl": null,
                        "location": "City 2",
                        "website": null,
                        "createdAt": "2024-01-15T10:31:00.000Z",
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
        
        // When: Get followers
        val result = followRepository.getFollowers(
            userId = 123L,
            page = 0,
            size = 10
        )
        
        // Then: Should return paginated followers
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        assertEquals(2, pagedData.items.size)
        assertEquals(0, pagedData.currentPage)
        assertEquals(10, pagedData.pageSize)
        assertEquals(2, pagedData.totalElements)
        assertEquals(1, pagedData.totalPages)
        assertTrue(pagedData.isLastPage)
        
        // Verify first follower
        val follower1 = pagedData.items[0]
        assertEquals(1L, follower1.id)
        assertEquals("follower1", follower1.username)
        assertEquals("Follower One", follower1.name)
        
        // Verify second follower
        val follower2 = pagedData.items[1]
        assertEquals(2L, follower2.id)
        assertEquals("follower2", follower2.username)
        assertEquals("Follower Two", follower2.name)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("follows/123/followers") == true)
        assertTrue(request.path?.contains("page=0") == true)
        assertTrue(request.path?.contains("size=10") == true)
    }
    
    /**
     * Test get following with pagination
     * Validates: Requirements 10.4
     */
    @Test
    fun `getFollowing returns paginated list of users being followed`() = runTest {
        // Given: Mock following response
        val responseJson = """
            {
                "content": [
                    {
                        "id": 10,
                        "email": "following1@example.com",
                        "username": "following1",
                        "name": "Following One",
                        "bio": "Bio 10",
                        "avatarUrl": "https://example.com/avatar10.jpg",
                        "location": "City 10",
                        "website": null,
                        "createdAt": "2024-01-15T10:30:00.000Z",
                        "updatedAt": null
                    },
                    {
                        "id": 11,
                        "email": "following2@example.com",
                        "username": "following2",
                        "name": "Following Two",
                        "bio": "Bio 11",
                        "avatarUrl": null,
                        "location": "City 11",
                        "website": "https://example11.com",
                        "createdAt": "2024-01-15T10:31:00.000Z",
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
        
        // When: Get following
        val result = followRepository.getFollowing(
            userId = 123L,
            page = 0,
            size = 10
        )
        
        // Then: Should return paginated following list
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        assertEquals(2, pagedData.items.size)
        assertEquals(0, pagedData.currentPage)
        assertEquals(10, pagedData.pageSize)
        assertEquals(2, pagedData.totalElements)
        assertEquals(1, pagedData.totalPages)
        assertTrue(pagedData.isLastPage)
        
        // Verify first user being followed
        val following1 = pagedData.items[0]
        assertEquals(10L, following1.id)
        assertEquals("following1", following1.username)
        assertEquals("Following One", following1.name)
        assertEquals("https://example.com/avatar10.jpg", following1.avatarUrl)
        
        // Verify second user being followed
        val following2 = pagedData.items[1]
        assertEquals(11L, following2.id)
        assertEquals("following2", following2.username)
        assertEquals("Following Two", following2.name)
        assertEquals("https://example11.com", following2.website)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("follows/123/following") == true)
        assertTrue(request.path?.contains("page=0") == true)
        assertTrue(request.path?.contains("size=10") == true)
    }
    
    /**
     * Test error scenario - user not found (404)
     * Validates: Requirements 10.3
     */
    @Test
    fun `checkFollowStatus with non-existent user returns not found error`() = runTest {
        // Given: Mock 404 Not Found response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 404,
                "error": "Not Found",
                "message": "User not found",
                "path": "/api/follows/check/999"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Check follow status for non-existent user
        val result = followRepository.checkFollowStatus(userId = 999L)
        
        // Then: Should return not found error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.NotFoundException)
        assertEquals("User not found", error.message)
    }
    
    /**
     * Test error scenario - authentication required (401)
     * Validates: Requirements 10.1
     */
    @Test
    fun `followUser without authentication returns authentication error`() = runTest {
        // Given: Mock 401 Unauthorized response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 401,
                "error": "Unauthorized",
                "message": "Authentication required",
                "path": "/api/follows/123"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Follow user without authentication
        val result = followRepository.followUser(userId = 123L)
        
        // Then: Should return authentication error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.AuthenticationException)
        assertEquals("Authentication required", error.message)
    }
    
    /**
     * Test pagination with multiple pages
     * Validates: Requirements 10.4
     */
    @Test
    fun `getFollowers with pagination returns correct page metadata`() = runTest {
        // Given: Mock response for page 1 of 3
        val responseJson = """
            {
                "content": [
                    {
                        "id": 20,
                        "email": "user20@example.com",
                        "username": "user20",
                        "name": "User Twenty",
                        "bio": "Bio 20",
                        "avatarUrl": null,
                        "location": "City 20",
                        "website": null,
                        "createdAt": "2024-01-15T10:30:00.000Z",
                        "updatedAt": null
                    }
                ],
                "pageable": {
                    "pageNumber": 1,
                    "pageSize": 5
                },
                "totalElements": 15,
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
        
        // When: Get followers page 1
        val result = followRepository.getFollowers(
            userId = 123L,
            page = 1,
            size = 5
        )
        
        // Then: Should return correct pagination metadata
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        assertEquals(1, pagedData.items.size)
        assertEquals(1, pagedData.currentPage)
        assertEquals(5, pagedData.pageSize)
        assertEquals(15, pagedData.totalElements)
        assertEquals(3, pagedData.totalPages)
        assertFalse(pagedData.isLastPage)
        
        // Verify request was made with correct page
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("page=1") == true)
        assertTrue(request.path?.contains("size=5") == true)
    }
    
    /**
     * Test empty followers list
     * Validates: Requirements 10.4
     */
    @Test
    fun `getFollowers with no followers returns empty list`() = runTest {
        // Given: Mock empty followers response
        val responseJson = """
            {
                "content": [],
                "pageable": {
                    "pageNumber": 0,
                    "pageSize": 10
                },
                "totalElements": 0,
                "totalPages": 0,
                "last": true
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get followers for user with no followers
        val result = followRepository.getFollowers(
            userId = 123L,
            page = 0,
            size = 10
        )
        
        // Then: Should return empty list
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        assertTrue(pagedData.items.isEmpty())
        assertEquals(0, pagedData.totalElements)
        assertEquals(0, pagedData.totalPages)
        assertTrue(pagedData.isLastPage)
    }
}
