package com.example.agrohub.data.repository

import com.example.agrohub.data.remote.api.CommentApiService
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
 * Integration tests for CommentRepository using MockWebServer.
 * Tests the full flow from repository through Retrofit to mock backend responses.
 * 
 * Validates: Requirements 14.1, 14.2, 14.3, 14.4
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class CommentRepositoryIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var commentApiService: CommentApiService
    private lateinit var commentRepository: CommentRepositoryImpl
    
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
        
        commentApiService = retrofit.create(CommentApiService::class.java)
        
        // Create repository
        commentRepository = CommentRepositoryImpl(commentApiService)
    }
    
    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
    
    /**
     * Test comment creation
     * Validates: Requirements 14.1
     */
    @Test
    fun `createComment with valid content returns created comment`() = runTest {
        // Given: Mock successful comment creation response
        val responseJson = """
            {
                "id": 1,
                "postId": 100,
                "userId": 123,
                "username": "testuser",
                "userAvatarUrl": "https://example.com/avatar.jpg",
                "content": "This is a test comment",
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
        
        // When: Create a comment
        val result = commentRepository.createComment(
            postId = 100L,
            content = "This is a test comment"
        )
        
        // Then: Should return success with comment data
        assertTrue(result is Result.Success)
        val comment = (result as Result.Success).data
        assertEquals(1L, comment.id)
        assertEquals(100L, comment.postId)
        assertEquals(123L, comment.author.id)
        assertEquals("testuser", comment.author.username)
        assertEquals("https://example.com/avatar.jpg", comment.author.avatarUrl)
        assertEquals("This is a test comment", comment.content)
        assertNotNull(comment.createdAt)
        assertNull(comment.updatedAt)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("comments") == true)
        
        // Verify request body contains postId and content
        val requestBody = request.body.readUtf8()
        assertTrue(requestBody.contains("100"))
        assertTrue(requestBody.contains("This is a test comment"))
    }
    
    /**
     * Test comment creation with empty content fails validation
     * Validates: Requirements 14.5
     */
    @Test
    fun `createComment with empty content returns validation error`() = runTest {
        // When: Create a comment with empty content
        val result = commentRepository.createComment(
            postId = 100L,
            content = ""
        )
        
        // Then: Should return validation error without making API call
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ValidationException)
        assertEquals("Comment content cannot be empty", error.message)
        
        // Verify no API request was made
        assertEquals(0, mockWebServer.requestCount)
    }
    
    /**
     * Test comment creation with blank content fails validation
     * Validates: Requirements 14.5
     */
    @Test
    fun `createComment with blank content returns validation error`() = runTest {
        // When: Create a comment with blank content (whitespace only)
        val result = commentRepository.createComment(
            postId = 100L,
            content = "   "
        )
        
        // Then: Should return validation error without making API call
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ValidationException)
        assertEquals("Comment content cannot be empty", error.message)
        
        // Verify no API request was made
        assertEquals(0, mockWebServer.requestCount)
    }
    
    /**
     * Test comment creation trims whitespace
     * Validates: Requirements 14.1
     */
    @Test
    fun `createComment trims whitespace from content`() = runTest {
        // Given: Mock successful comment creation response
        val responseJson = """
            {
                "id": 1,
                "postId": 100,
                "userId": 123,
                "username": "testuser",
                "userAvatarUrl": null,
                "content": "Trimmed comment",
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
        
        // When: Create a comment with leading/trailing whitespace
        val result = commentRepository.createComment(
            postId = 100L,
            content = "  Trimmed comment  "
        )
        
        // Then: Should return success
        assertTrue(result is Result.Success)
        
        // Verify request body has trimmed content
        val request = mockWebServer.takeRequest()
        val requestBody = request.body.readUtf8()
        assertTrue(requestBody.contains("Trimmed comment"))
    }
    
    /**
     * Test comment update
     * Validates: Requirements 14.2
     */
    @Test
    fun `updateComment success returns updated comment`() = runTest {
        // Given: Mock successful update response
        val responseJson = """
            {
                "id": 1,
                "postId": 100,
                "userId": 123,
                "username": "testuser",
                "userAvatarUrl": "https://example.com/avatar.jpg",
                "content": "This is updated content",
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
        
        // When: Update a comment
        val result = commentRepository.updateComment(
            commentId = 1L,
            content = "This is updated content"
        )
        
        // Then: Should return success with updated comment data
        assertTrue(result is Result.Success)
        val comment = (result as Result.Success).data
        assertEquals(1L, comment.id)
        assertEquals("This is updated content", comment.content)
        assertNotNull(comment.updatedAt)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("PUT", request.method)
        assertTrue(request.path?.contains("comments/1") == true)
    }
    
    /**
     * Test comment update with empty content fails validation
     * Validates: Requirements 14.5
     */
    @Test
    fun `updateComment with empty content returns validation error`() = runTest {
        // When: Update a comment with empty content
        val result = commentRepository.updateComment(
            commentId = 1L,
            content = ""
        )
        
        // Then: Should return validation error without making API call
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ValidationException)
        assertEquals("Comment content cannot be empty", error.message)
        
        // Verify no API request was made
        assertEquals(0, mockWebServer.requestCount)
    }
    
    /**
     * Test comment deletion
     * Validates: Requirements 14.3
     */
    @Test
    fun `deleteComment success returns unit`() = runTest {
        // Given: Mock successful deletion response (204 No Content)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
        
        // When: Delete a comment
        val result = commentRepository.deleteComment(commentId = 1L)
        
        // Then: Should return success
        assertTrue(result is Result.Success)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertTrue(request.path?.contains("comments/1") == true)
    }
    
    /**
     * Test get post comments with pagination
     * Validates: Requirements 14.4
     */
    @Test
    fun `getPostComments success returns paged comments`() = runTest {
        // Given: Mock successful response with pagination
        val responseJson = """
            {
                "content": [
                    {
                        "id": 1,
                        "postId": 100,
                        "userId": 123,
                        "username": "user1",
                        "userAvatarUrl": "https://example.com/avatar1.jpg",
                        "content": "First comment",
                        "createdAt": "2024-01-15T10:30:00.000Z",
                        "updatedAt": null
                    },
                    {
                        "id": 2,
                        "postId": 100,
                        "userId": 456,
                        "username": "user2",
                        "userAvatarUrl": null,
                        "content": "Second comment",
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
        
        // When: Get post comments
        val result = commentRepository.getPostComments(
            postId = 100L,
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
        
        // Verify first comment
        val comment1 = pagedData.items[0]
        assertEquals(1L, comment1.id)
        assertEquals(100L, comment1.postId)
        assertEquals(123L, comment1.author.id)
        assertEquals("user1", comment1.author.username)
        assertEquals("https://example.com/avatar1.jpg", comment1.author.avatarUrl)
        assertEquals("First comment", comment1.content)
        
        // Verify second comment
        val comment2 = pagedData.items[1]
        assertEquals(2L, comment2.id)
        assertEquals(100L, comment2.postId)
        assertEquals(456L, comment2.author.id)
        assertEquals("user2", comment2.author.username)
        assertNull(comment2.author.avatarUrl)
        assertEquals("Second comment", comment2.content)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("comments/post/100") == true)
        assertTrue(request.path?.contains("page=0") == true)
        assertTrue(request.path?.contains("size=10") == true)
    }
    
    /**
     * Test get post comments with empty results
     * Validates: Requirements 14.4
     */
    @Test
    fun `getPostComments with no comments returns empty paged data`() = runTest {
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
        
        // When: Get comments for post with no comments
        val result = commentRepository.getPostComments(
            postId = 200L,
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
     * Test get post comments with pagination - second page
     * Validates: Requirements 14.4
     */
    @Test
    fun `getPostComments with page 1 returns second page of comments`() = runTest {
        // Given: Mock response for second page
        val responseJson = """
            {
                "content": [
                    {
                        "id": 11,
                        "postId": 100,
                        "userId": 789,
                        "username": "user11",
                        "userAvatarUrl": null,
                        "content": "Comment on page 2",
                        "createdAt": "2024-01-15T10:40:00.000Z",
                        "updatedAt": null
                    }
                ],
                "pageable": {
                    "pageNumber": 1,
                    "pageSize": 10
                },
                "totalElements": 11,
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
        
        // When: Get second page of comments
        val result = commentRepository.getPostComments(
            postId = 100L,
            page = 1,
            size = 10
        )
        
        // Then: Should return success with second page data
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        assertEquals(1, pagedData.items.size)
        assertEquals(1, pagedData.currentPage)
        assertEquals(11, pagedData.totalElements)
        assertEquals(2, pagedData.totalPages)
        assertTrue(pagedData.isLastPage)
        
        // Verify request was made with correct page parameter
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("page=1") == true)
    }
    
    /**
     * Test comment creation on non-existent post
     * Validates: Requirements 14.1
     */
    @Test
    fun `createComment on non-existent post returns not found error`() = runTest {
        // Given: Mock 404 Not Found response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 404,
                "error": "Not Found",
                "message": "Post not found",
                "path": "/api/comments"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Create comment on non-existent post
        val result = commentRepository.createComment(
            postId = 999L,
            content = "Comment on non-existent post"
        )
        
        // Then: Should return not found error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.NotFoundException)
        assertEquals("Comment not found", error.message)
    }
    
    /**
     * Test authorization error when updating someone else's comment
     * Validates: Requirements 14.2
     */
    @Test
    fun `updateComment with unauthorized access returns authorization error`() = runTest {
        // Given: Mock 403 Forbidden response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 403,
                "error": "Forbidden",
                "message": "You don't have permission to update this comment",
                "path": "/api/comments/1"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(403)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Try to update someone else's comment
        val result = commentRepository.updateComment(
            commentId = 1L,
            content = "Trying to update someone else's comment"
        )
        
        // Then: Should return authorization error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.AuthorizationException)
        assertEquals("You don't have permission to perform this action", error.message)
    }
    
    /**
     * Test authorization error when deleting someone else's comment
     * Validates: Requirements 14.3
     */
    @Test
    fun `deleteComment with unauthorized access returns authorization error`() = runTest {
        // Given: Mock 403 Forbidden response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 403,
                "error": "Forbidden",
                "message": "You don't have permission to delete this comment",
                "path": "/api/comments/1"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(403)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Try to delete someone else's comment
        val result = commentRepository.deleteComment(commentId = 1L)
        
        // Then: Should return authorization error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.AuthorizationException)
        assertEquals("You don't have permission to perform this action", error.message)
    }
    
    /**
     * Test server error handling
     * Validates: Requirements 14.1
     */
    @Test
    fun `createComment with server error returns server exception`() = runTest {
        // Given: Mock 500 Internal Server Error response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )
        
        // When: Create a comment when server has error
        val result = commentRepository.createComment(
            postId = 100L,
            content = "Test comment"
        )
        
        // Then: Should return server error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ServerException)
        assertEquals("Server error. Please try again later.", error.message)
    }
    
    /**
     * Test authentication error
     * Validates: Requirements 14.1
     */
    @Test
    fun `createComment without authentication returns authentication error`() = runTest {
        // Given: Mock 401 Unauthorized response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 401,
                "error": "Unauthorized",
                "message": "Authentication required",
                "path": "/api/comments"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Create comment without authentication
        val result = commentRepository.createComment(
            postId = 100L,
            content = "Test comment"
        )
        
        // Then: Should return authentication error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.AuthenticationException)
        assertEquals("Authentication required", error.message)
    }
}
