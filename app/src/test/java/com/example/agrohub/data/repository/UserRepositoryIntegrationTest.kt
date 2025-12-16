package com.example.agrohub.data.repository

import com.example.agrohub.data.cache.MemoryCache
import com.example.agrohub.data.remote.api.UserApiService
import com.example.agrohub.domain.model.User
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
 * Integration tests for UserRepository using MockWebServer.
 * Tests the full flow from repository through Retrofit to mock backend responses.
 * 
 * Validates: Requirements 8.1, 8.2, 9.1, 22.1
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class UserRepositoryIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var userApiService: UserApiService
    private lateinit var userCache: MemoryCache<Long, User>
    private lateinit var userRepository: UserRepositoryImpl
    
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
        
        userApiService = retrofit.create(UserApiService::class.java)
        
        // Create cache with short TTL for testing
        userCache = MemoryCache(maxSize = 10, ttlMillis = 1000)
        
        // Create repository
        userRepository = UserRepositoryImpl(userApiService, userCache)
    }
    
    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
    
    /**
     * Test profile creation and retrieval
     * Validates: Requirements 8.1
     */
    @Test
    fun `createProfile success returns user and caches it`() = runTest {
        // Given: Mock successful profile creation response
        val responseJson = """
            {
                "id": 123,
                "email": "test@example.com",
                "username": "testuser",
                "name": "Test User",
                "bio": "Test bio",
                "avatarUrl": "https://example.com/avatar.jpg",
                "location": "Test City",
                "website": "https://example.com",
                "createdAt": "2024-01-15T10:30:00.000Z",
                "updatedAt": "2024-01-15T10:30:00.000Z"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Create a profile
        val result = userRepository.createProfile(
            name = "Test User",
            bio = "Test bio",
            avatarUrl = "https://example.com/avatar.jpg",
            location = "Test City",
            website = "https://example.com"
        )
        
        // Then: Should return success with user data
        assertTrue(result is Result.Success)
        val user = (result as Result.Success).data
        assertEquals(123L, user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("testuser", user.username)
        assertEquals("Test User", user.name)
        assertEquals("Test bio", user.bio)
        assertEquals("https://example.com/avatar.jpg", user.avatarUrl)
        assertEquals("Test City", user.location)
        assertEquals("https://example.com", user.website)
        
        // Verify user is cached
        assertTrue(userCache.containsKey(123L))
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("users") == true)
    }
    
    /**
     * Test profile update with cache invalidation
     * Validates: Requirements 8.2, 22.1
     */
    @Test
    fun `updateProfile success returns updated user and invalidates cache`() = runTest {
        // Given: Pre-populate cache with old user data
        val oldUser = User(
            id = 123,
            email = "test@example.com",
            username = "testuser",
            name = "Old Name",
            bio = "Old bio",
            avatarUrl = null,
            location = "",
            website = null,
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = null
        )
        userCache.put(123L, oldUser)
        
        // Mock successful update response
        val responseJson = """
            {
                "id": 123,
                "email": "test@example.com",
                "username": "testuser",
                "name": "Updated Name",
                "bio": "Updated bio",
                "avatarUrl": "https://example.com/new-avatar.jpg",
                "location": "New City",
                "website": "https://newsite.com",
                "createdAt": "2024-01-15T10:30:00.000Z",
                "updatedAt": "2024-01-15T11:00:00.000Z"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Update profile
        val result = userRepository.updateProfile(
            userId = 123L,
            name = "Updated Name",
            bio = "Updated bio",
            avatarUrl = "https://example.com/new-avatar.jpg",
            location = "New City",
            website = "https://newsite.com"
        )
        
        // Then: Should return success with updated user data
        assertTrue(result is Result.Success)
        val user = (result as Result.Success).data
        assertEquals(123L, user.id)
        assertEquals("Updated Name", user.name)
        assertEquals("Updated bio", user.bio)
        assertEquals("https://example.com/new-avatar.jpg", user.avatarUrl)
        assertEquals("New City", user.location)
        assertEquals("https://newsite.com", user.website)
        
        // Verify cache was updated with new data
        val cachedUser = userCache.get(123L)
        assertNotNull(cachedUser)
        assertEquals("Updated Name", cachedUser?.name)
        assertEquals("Updated bio", cachedUser?.bio)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("PUT", request.method)
        assertTrue(request.path?.contains("users/123") == true)
    }
    
    /**
     * Test user search with pagination
     * Validates: Requirements 9.1
     */
    @Test
    fun `searchUsers success returns paged results and caches users`() = runTest {
        // Given: Mock successful search response
        val responseJson = """
            {
                "content": [
                    {
                        "id": 1,
                        "email": "user1@example.com",
                        "username": "user1",
                        "name": "User One",
                        "bio": "Bio 1",
                        "avatarUrl": null,
                        "location": "City 1",
                        "website": null,
                        "createdAt": "2024-01-15T10:30:00.000Z",
                        "updatedAt": null
                    },
                    {
                        "id": 2,
                        "email": "user2@example.com",
                        "username": "user2",
                        "name": "User Two",
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
        
        // When: Search for users
        val result = userRepository.searchUsers(
            query = "user",
            page = 0,
            size = 10
        )
        
        // Then: Should return success with paged data
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        assertEquals(2, pagedData.items.size)
        assertEquals(0, pagedData.currentPage)
        assertEquals(10, pagedData.pageSize)
        assertEquals(2, pagedData.totalElements)
        assertEquals(1, pagedData.totalPages)
        assertTrue(pagedData.isLastPage)
        
        // Verify first user
        val user1 = pagedData.items[0]
        assertEquals(1L, user1.id)
        assertEquals("user1", user1.username)
        assertEquals("User One", user1.name)
        
        // Verify second user
        val user2 = pagedData.items[1]
        assertEquals(2L, user2.id)
        assertEquals("user2", user2.username)
        assertEquals("User Two", user2.name)
        
        // Verify users are cached
        assertTrue(userCache.containsKey(1L))
        assertTrue(userCache.containsKey(2L))
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("users/search") == true)
        assertTrue(request.path?.contains("query=user") == true)
        assertTrue(request.path?.contains("page=0") == true)
        assertTrue(request.path?.contains("size=10") == true)
    }
    
    /**
     * Test getUserById with caching behavior
     * Validates: Requirements 8.4, 22.1
     */
    @Test
    fun `getUserById returns cached user without API call`() = runTest {
        // Given: Pre-populate cache
        val cachedUser = User(
            id = 123,
            email = "cached@example.com",
            username = "cacheduser",
            name = "Cached User",
            bio = "Cached bio",
            avatarUrl = null,
            location = "Cache City",
            website = null,
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = null
        )
        userCache.put(123L, cachedUser)
        
        // When: Get user by ID (should use cache)
        val result = userRepository.getUserById(123L)
        
        // Then: Should return cached user without API call
        assertTrue(result is Result.Success)
        val user = (result as Result.Success).data
        assertEquals(123L, user.id)
        assertEquals("cacheduser", user.username)
        assertEquals("Cached User", user.name)
        
        // Verify no API request was made
        assertEquals(0, mockWebServer.requestCount)
    }
    
    /**
     * Test getUserById fetches from API on cache miss
     * Validates: Requirements 8.4, 22.1
     */
    @Test
    fun `getUserById fetches from API on cache miss and caches result`() = runTest {
        // Given: Cache is empty, mock API response
        val responseJson = """
            {
                "id": 456,
                "email": "api@example.com",
                "username": "apiuser",
                "name": "API User",
                "bio": "API bio",
                "avatarUrl": "https://example.com/api-avatar.jpg",
                "location": "API City",
                "website": null,
                "createdAt": "2024-01-15T10:30:00.000Z",
                "updatedAt": null
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get user by ID (cache miss)
        val result = userRepository.getUserById(456L)
        
        // Then: Should return user from API
        assertTrue(result is Result.Success)
        val user = (result as Result.Success).data
        assertEquals(456L, user.id)
        assertEquals("apiuser", user.username)
        assertEquals("API User", user.name)
        
        // Verify user is now cached
        assertTrue(userCache.containsKey(456L))
        val cachedUser = userCache.get(456L)
        assertNotNull(cachedUser)
        assertEquals("apiuser", cachedUser?.username)
        
        // Verify API request was made
        assertEquals(1, mockWebServer.requestCount)
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("users/456") == true)
    }
    
    /**
     * Test getCurrentUser success
     * Validates: Requirements 8.3
     */
    @Test
    fun `getCurrentUser success returns current user and caches it`() = runTest {
        // Given: Mock successful response
        val responseJson = """
            {
                "id": 789,
                "email": "current@example.com",
                "username": "currentuser",
                "name": "Current User",
                "bio": "Current bio",
                "avatarUrl": null,
                "location": "Current City",
                "website": "https://current.com",
                "createdAt": "2024-01-15T10:30:00.000Z",
                "updatedAt": null
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get current user
        val result = userRepository.getCurrentUser()
        
        // Then: Should return success with user data
        assertTrue(result is Result.Success)
        val user = (result as Result.Success).data
        assertEquals(789L, user.id)
        assertEquals("currentuser", user.username)
        assertEquals("Current User", user.name)
        
        // Verify user is cached
        assertTrue(userCache.containsKey(789L))
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("users/me") == true)
    }
    
    /**
     * Test getUserByUsername success
     * Validates: Requirements 9.2
     */
    @Test
    fun `getUserByUsername success returns user and caches it`() = runTest {
        // Given: Mock successful response
        val responseJson = """
            {
                "id": 999,
                "email": "username@example.com",
                "username": "searcheduser",
                "name": "Searched User",
                "bio": "Searched bio",
                "avatarUrl": null,
                "location": "Search City",
                "website": null,
                "createdAt": "2024-01-15T10:30:00.000Z",
                "updatedAt": null
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get user by username
        val result = userRepository.getUserByUsername("searcheduser")
        
        // Then: Should return success with user data
        assertTrue(result is Result.Success)
        val user = (result as Result.Success).data
        assertEquals(999L, user.id)
        assertEquals("searcheduser", user.username)
        assertEquals("Searched User", user.name)
        
        // Verify user is cached
        assertTrue(userCache.containsKey(999L))
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("users/username/searcheduser") == true)
    }
    
    /**
     * Test error scenario - user not found (404)
     * Validates: Requirements 8.4
     */
    @Test
    fun `getUserById with non-existent user returns not found error`() = runTest {
        // Given: Mock 404 Not Found response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 404,
                "error": "Not Found",
                "message": "User not found",
                "path": "/api/users/999"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get non-existent user
        val result = userRepository.getUserById(999L)
        
        // Then: Should return not found error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.NotFoundException)
        assertEquals("User not found", error.message)
    }
    
    /**
     * Test error scenario - validation error (400)
     * Validates: Requirements 8.1
     */
    @Test
    fun `createProfile with invalid data returns validation error`() = runTest {
        // Given: Mock 400 Bad Request response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 400,
                "error": "Bad Request",
                "message": "Invalid profile data",
                "path": "/api/users"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Create profile with invalid data
        val result = userRepository.createProfile(
            name = null,
            bio = null,
            avatarUrl = null,
            location = null,
            website = null
        )
        
        // Then: Should return validation error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ValidationException)
    }
    
    /**
     * Test search with empty results
     * Validates: Requirements 9.4
     */
    @Test
    fun `searchUsers with no results returns empty paged data`() = runTest {
        // Given: Mock empty search response
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
        
        // When: Search with no results
        val result = userRepository.searchUsers(
            query = "nonexistent",
            page = 0,
            size = 10
        )
        
        // Then: Should return success with empty list
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        assertTrue(pagedData.items.isEmpty())
        assertEquals(0, pagedData.totalElements)
        assertEquals(0, pagedData.totalPages)
        assertTrue(pagedData.isLastPage)
    }
}
