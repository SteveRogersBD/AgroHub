package com.example.agrohub.data.repository

import com.example.agrohub.data.remote.api.PostApiService
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
 * Integration tests for PostRepository using MockWebServer.
 * Tests the full flow from repository through Retrofit to mock backend responses.
 * 
 * Validates: Requirements 11.1, 11.2, 11.3, 11.4
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class PostRepositoryIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var postApiService: PostApiService
    private lateinit var postRepository: PostRepositoryImpl
    
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
        
        postApiService = retrofit.create(PostApiService::class.java)
        
        // Create repository
        postRepository = PostRepositoryImpl(postApiService)
    }
    
    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
    
    /**
     * Test post creation with content and media
     * Validates: Requirements 11.1
     */
    @Test
    fun `createPost with content and media returns created post`() = runTest {
        // Given: Mock successful post creation response
        val responseJson = """
            {
                "id": 1,
                "userId": 123,
                "content": "This is a test post with media",
                "mediaUrl": "https://example.com/image.jpg",
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
        
        // When: Create a post with content and media
        val result = postRepository.createPost(
            content = "This is a test post with media",
            mediaUrl = "https://example.com/image.jpg"
        )
        
        // Then: Should return success with post data
        assertTrue(result is Result.Success)
        val post = (result as Result.Success).data
        assertEquals(1L, post.id)
        assertEquals(123L, post.userId)
        assertEquals("This is a test post with media", post.content)
        assertEquals("https://example.com/image.jpg", post.mediaUrl)
        assertNotNull(post.createdAt)
        assertNull(post.updatedAt)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("posts") == true)
        
        // Verify request body contains content and mediaUrl
        val requestBody = request.body.readUtf8()
        assertTrue(requestBody.contains("This is a test post with media"))
        assertTrue(requestBody.contains("https://example.com/image.jpg"))
    }
    
    /**
     * Test post creation with content only (no media)
     * Validates: Requirements 11.1
     */
    @Test
    fun `createPost with content only returns created post`() = runTest {
        // Given: Mock successful post creation response
        val responseJson = """
            {
                "id": 2,
                "userId": 123,
                "content": "This is a text-only post",
                "mediaUrl": null,
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
        
        // When: Create a post without media
        val result = postRepository.createPost(
            content = "This is a text-only post",
            mediaUrl = null
        )
        
        // Then: Should return success with post data
        assertTrue(result is Result.Success)
        val post = (result as Result.Success).data
        assertEquals(2L, post.id)
        assertEquals("This is a text-only post", post.content)
        assertNull(post.mediaUrl)
    }
    
    /**
     * Test post creation with empty content fails validation
     * Validates: Requirements 11.5
     */
    @Test
    fun `createPost with empty content returns validation error`() = runTest {
        // When: Create a post with empty content
        val result = postRepository.createPost(
            content = "",
            mediaUrl = null
        )
        
        // Then: Should return validation error without making API call
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ValidationException)
        assertEquals("Post content cannot be empty", error.message)
        
        // Verify no API request was made
        assertEquals(0, mockWebServer.requestCount)
    }
    
    /**
     * Test post creation with blank content fails validation
     * Validates: Requirements 11.5
     */
    @Test
    fun `createPost with blank content returns validation error`() = runTest {
        // When: Create a post with blank content (whitespace only)
        val result = postRepository.createPost(
            content = "   ",
            mediaUrl = null
        )
        
        // Then: Should return validation error without making API call
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ValidationException)
        assertEquals("Post content cannot be empty", error.message)
        
        // Verify no API request was made
        assertEquals(0, mockWebServer.requestCount)
    }
    
    /**
     * Test post update
     * Validates: Requirements 11.2
     */
    @Test
    fun `updatePost success returns updated post`() = runTest {
        // Given: Mock successful update response
        val responseJson = """
            {
                "id": 1,
                "userId": 123,
                "content": "This is updated content",
                "mediaUrl": "https://example.com/new-image.jpg",
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
        
        // When: Update a post
        val result = postRepository.updatePost(
            postId = 1L,
            content = "This is updated content",
            mediaUrl = "https://example.com/new-image.jpg"
        )
        
        // Then: Should return success with updated post data
        assertTrue(result is Result.Success)
        val post = (result as Result.Success).data
        assertEquals(1L, post.id)
        assertEquals("This is updated content", post.content)
        assertEquals("https://example.com/new-image.jpg", post.mediaUrl)
        assertNotNull(post.updatedAt)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("PUT", request.method)
        assertTrue(request.path?.contains("posts/1") == true)
    }
    
    /**
     * Test post update with empty content fails validation
     * Validates: Requirements 11.5
     */
    @Test
    fun `updatePost with empty content returns validation error`() = runTest {
        // When: Update a post with empty content
        val result = postRepository.updatePost(
            postId = 1L,
            content = "",
            mediaUrl = null
        )
        
        // Then: Should return validation error without making API call
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ValidationException)
        assertEquals("Post content cannot be empty", error.message)
        
        // Verify no API request was made
        assertEquals(0, mockWebServer.requestCount)
    }
    
    /**
     * Test post deletion
     * Validates: Requirements 11.3
     */
    @Test
    fun `deletePost success returns unit`() = runTest {
        // Given: Mock successful deletion response (204 No Content)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
        
        // When: Delete a post
        val result = postRepository.deletePost(postId = 1L)
        
        // Then: Should return success
        assertTrue(result is Result.Success)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertTrue(request.path?.contains("posts/1") == true)
    }
    
    /**
     * Test get post by ID
     * Validates: Requirements 11.4
     */
    @Test
    fun `getPostById success returns post`() = runTest {
        // Given: Mock successful response
        val responseJson = """
            {
                "id": 1,
                "userId": 123,
                "content": "This is a test post",
                "mediaUrl": "https://example.com/image.jpg",
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
        
        // When: Get post by ID
        val result = postRepository.getPostById(postId = 1L)
        
        // Then: Should return success with post data
        assertTrue(result is Result.Success)
        val post = (result as Result.Success).data
        assertEquals(1L, post.id)
        assertEquals(123L, post.userId)
        assertEquals("This is a test post", post.content)
        assertEquals("https://example.com/image.jpg", post.mediaUrl)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("posts/1") == true)
    }
    
    /**
     * Test get post by ID with non-existent post
     * Validates: Requirements 11.4
     */
    @Test
    fun `getPostById with non-existent post returns not found error`() = runTest {
        // Given: Mock 404 Not Found response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 404,
                "error": "Not Found",
                "message": "Post not found",
                "path": "/api/posts/999"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get non-existent post
        val result = postRepository.getPostById(postId = 999L)
        
        // Then: Should return not found error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.NotFoundException)
        assertEquals("Post not found", error.message)
    }
    
    /**
     * Test get user posts with pagination
     * Validates: Requirements 11.4
     */
    @Test
    fun `getUserPosts success returns paged posts`() = runTest {
        // Given: Mock successful response with pagination
        val responseJson = """
            {
                "content": [
                    {
                        "id": 1,
                        "userId": 123,
                        "content": "First post",
                        "mediaUrl": null,
                        "createdAt": "2024-01-15T10:30:00.000Z",
                        "updatedAt": null
                    },
                    {
                        "id": 2,
                        "userId": 123,
                        "content": "Second post",
                        "mediaUrl": "https://example.com/image.jpg",
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
        
        // When: Get user posts
        val result = postRepository.getUserPosts(
            userId = 123L,
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
        
        // Verify first post
        val post1 = pagedData.items[0]
        assertEquals(1L, post1.id)
        assertEquals(123L, post1.userId)
        assertEquals("First post", post1.content)
        assertNull(post1.mediaUrl)
        
        // Verify second post
        val post2 = pagedData.items[1]
        assertEquals(2L, post2.id)
        assertEquals(123L, post2.userId)
        assertEquals("Second post", post2.content)
        assertEquals("https://example.com/image.jpg", post2.mediaUrl)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("posts/user/123") == true)
        assertTrue(request.path?.contains("page=0") == true)
        assertTrue(request.path?.contains("size=10") == true)
    }
    
    /**
     * Test get user posts with empty results
     * Validates: Requirements 11.4
     */
    @Test
    fun `getUserPosts with no posts returns empty paged data`() = runTest {
        // Given: Mock empty response
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
        
        // When: Get user posts for user with no posts
        val result = postRepository.getUserPosts(
            userId = 456L,
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
    
    /**
     * Test authorization error when updating someone else's post
     * Validates: Requirements 11.2
     */
    @Test
    fun `updatePost with unauthorized access returns authorization error`() = runTest {
        // Given: Mock 403 Forbidden response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 403,
                "error": "Forbidden",
                "message": "You don't have permission to update this post",
                "path": "/api/posts/1"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(403)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Try to update someone else's post
        val result = postRepository.updatePost(
            postId = 1L,
            content = "Trying to update someone else's post",
            mediaUrl = null
        )
        
        // Then: Should return authorization error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.AuthorizationException)
        assertEquals("You don't have permission to perform this action", error.message)
    }
    
    /**
     * Test authorization error when deleting someone else's post
     * Validates: Requirements 11.3
     */
    @Test
    fun `deletePost with unauthorized access returns authorization error`() = runTest {
        // Given: Mock 403 Forbidden response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 403,
                "error": "Forbidden",
                "message": "You don't have permission to delete this post",
                "path": "/api/posts/1"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(403)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Try to delete someone else's post
        val result = postRepository.deletePost(postId = 1L)
        
        // Then: Should return authorization error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.AuthorizationException)
        assertEquals("You don't have permission to perform this action", error.message)
    }
    
    /**
     * Test server error handling
     * Validates: Requirements 11.1
     */
    @Test
    fun `createPost with server error returns server exception`() = runTest {
        // Given: Mock 500 Internal Server Error response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )
        
        // When: Create a post when server has error
        val result = postRepository.createPost(
            content = "Test post",
            mediaUrl = null
        )
        
        // Then: Should return server error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ServerException)
        assertEquals("Server error. Please try again later.", error.message)
    }
}
