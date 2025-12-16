package com.example.agrohub.data.repository

import com.example.agrohub.data.remote.api.NotificationApiService
import com.example.agrohub.domain.model.NotificationType
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
 * Integration tests for NotificationRepository using MockWebServer.
 * Tests the full flow from repository through Retrofit to mock backend responses.
 * 
 * Validates: Requirements 15.1, 15.2, 15.3, 15.4, 15.5
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class NotificationRepositoryIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var notificationApiService: NotificationApiService
    private lateinit var notificationRepository: NotificationRepositoryImpl
    
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
        
        notificationApiService = retrofit.create(NotificationApiService::class.java)
        
        // Create repository
        notificationRepository = NotificationRepositoryImpl(notificationApiService)
    }
    
    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
    
    /**
     * Test notification retrieval with pagination
     * Validates: Requirements 15.1
     */
    @Test
    fun `getNotifications success returns paged notifications`() = runTest {
        // Given: Mock successful response with pagination
        val responseJson = """
            {
                "content": [
                    {
                        "id": 1,
                        "userId": 123,
                        "type": "LIKE",
                        "actorId": 456,
                        "actorUsername": "john_doe",
                        "actorAvatarUrl": "https://example.com/avatar1.jpg",
                        "postId": 100,
                        "message": "john_doe liked your post",
                        "isRead": false,
                        "createdAt": "2024-01-15T10:30:00.000Z"
                    },
                    {
                        "id": 2,
                        "userId": 123,
                        "type": "COMMENT",
                        "actorId": 789,
                        "actorUsername": "jane_smith",
                        "actorAvatarUrl": null,
                        "postId": 100,
                        "message": "jane_smith commented on your post",
                        "isRead": true,
                        "createdAt": "2024-01-15T09:30:00.000Z"
                    },
                    {
                        "id": 3,
                        "userId": 123,
                        "type": "FOLLOW",
                        "actorId": 999,
                        "actorUsername": "bob_wilson",
                        "actorAvatarUrl": "https://example.com/avatar3.jpg",
                        "postId": null,
                        "message": "bob_wilson started following you",
                        "isRead": false,
                        "createdAt": "2024-01-15T08:30:00.000Z"
                    }
                ],
                "pageable": {
                    "pageNumber": 0,
                    "pageSize": 10
                },
                "totalElements": 3,
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
        
        // When: Get notifications
        val result = notificationRepository.getNotifications(page = 0, size = 10)
        
        // Then: Should return success with paged data
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        assertEquals(3, pagedData.items.size)
        assertEquals(0, pagedData.currentPage)
        assertEquals(10, pagedData.pageSize)
        assertEquals(3, pagedData.totalElements)
        assertEquals(1, pagedData.totalPages)
        assertTrue(pagedData.isLastPage)
        
        // Verify first notification (LIKE)
        val notification1 = pagedData.items[0]
        assertEquals(1L, notification1.id)
        assertEquals(NotificationType.LIKE, notification1.type)
        assertEquals(456L, notification1.actor.id)
        assertEquals("john_doe", notification1.actor.username)
        assertEquals("https://example.com/avatar1.jpg", notification1.actor.avatarUrl)
        assertEquals(100L, notification1.postId)
        assertEquals("john_doe liked your post", notification1.message)
        assertFalse(notification1.isRead)
        assertNotNull(notification1.createdAt)
        
        // Verify second notification (COMMENT)
        val notification2 = pagedData.items[1]
        assertEquals(2L, notification2.id)
        assertEquals(NotificationType.COMMENT, notification2.type)
        assertEquals(789L, notification2.actor.id)
        assertEquals("jane_smith", notification2.actor.username)
        assertNull(notification2.actor.avatarUrl)
        assertEquals(100L, notification2.postId)
        assertEquals("jane_smith commented on your post", notification2.message)
        assertTrue(notification2.isRead)
        
        // Verify third notification (FOLLOW)
        val notification3 = pagedData.items[2]
        assertEquals(3L, notification3.id)
        assertEquals(NotificationType.FOLLOW, notification3.type)
        assertEquals(999L, notification3.actor.id)
        assertEquals("bob_wilson", notification3.actor.username)
        assertEquals("https://example.com/avatar3.jpg", notification3.actor.avatarUrl)
        assertNull(notification3.postId) // FOLLOW notifications don't have postId
        assertEquals("bob_wilson started following you", notification3.message)
        assertFalse(notification3.isRead)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("notifications") == true)
        assertTrue(request.path?.contains("page=0") == true)
        assertTrue(request.path?.contains("size=10") == true)
    }
    
    /**
     * Test notification retrieval with empty results
     * Validates: Requirements 15.1
     */
    @Test
    fun `getNotifications with no notifications returns empty paged data`() = runTest {
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
        
        // When: Get notifications when there are none
        val result = notificationRepository.getNotifications(page = 0, size = 10)
        
        // Then: Should return success with empty list
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        assertTrue(pagedData.items.isEmpty())
        assertEquals(0, pagedData.totalElements)
        assertEquals(0, pagedData.totalPages)
        assertTrue(pagedData.isLastPage)
    }
    
    /**
     * Test notification retrieval with pagination - second page
     * Validates: Requirements 15.1
     */
    @Test
    fun `getNotifications with page 1 returns second page of notifications`() = runTest {
        // Given: Mock response for second page
        val responseJson = """
            {
                "content": [
                    {
                        "id": 11,
                        "userId": 123,
                        "type": "LIKE",
                        "actorId": 555,
                        "actorUsername": "user11",
                        "actorAvatarUrl": null,
                        "postId": 200,
                        "message": "user11 liked your post",
                        "isRead": true,
                        "createdAt": "2024-01-14T10:30:00.000Z"
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
        
        // When: Get second page of notifications
        val result = notificationRepository.getNotifications(page = 1, size = 10)
        
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
     * Test unread notifications filtering
     * Validates: Requirements 15.2
     */
    @Test
    fun `getUnreadNotifications success returns only unread notifications`() = runTest {
        // Given: Mock successful response with only unread notifications
        val responseJson = """
            {
                "content": [
                    {
                        "id": 1,
                        "userId": 123,
                        "type": "LIKE",
                        "actorId": 456,
                        "actorUsername": "john_doe",
                        "actorAvatarUrl": "https://example.com/avatar1.jpg",
                        "postId": 100,
                        "message": "john_doe liked your post",
                        "isRead": false,
                        "createdAt": "2024-01-15T10:30:00.000Z"
                    },
                    {
                        "id": 3,
                        "userId": 123,
                        "type": "FOLLOW",
                        "actorId": 999,
                        "actorUsername": "bob_wilson",
                        "actorAvatarUrl": "https://example.com/avatar3.jpg",
                        "postId": null,
                        "message": "bob_wilson started following you",
                        "isRead": false,
                        "createdAt": "2024-01-15T08:30:00.000Z"
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
        
        // When: Get unread notifications
        val result = notificationRepository.getUnreadNotifications(page = 0, size = 10)
        
        // Then: Should return success with only unread notifications
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        assertEquals(2, pagedData.items.size)
        
        // Verify all notifications are unread
        pagedData.items.forEach { notification ->
            assertFalse("All notifications should be unread", notification.isRead)
        }
        
        // Verify request was made to unread endpoint
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("notifications/unread") == true)
        assertTrue(request.path?.contains("page=0") == true)
        assertTrue(request.path?.contains("size=10") == true)
    }
    
    /**
     * Test unread notifications with empty results
     * Validates: Requirements 15.2
     */
    @Test
    fun `getUnreadNotifications with no unread notifications returns empty paged data`() = runTest {
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
        
        // When: Get unread notifications when all are read
        val result = notificationRepository.getUnreadNotifications(page = 0, size = 10)
        
        // Then: Should return success with empty list
        assertTrue(result is Result.Success)
        val pagedData = (result as Result.Success).data
        assertTrue(pagedData.items.isEmpty())
        assertEquals(0, pagedData.totalElements)
    }
    
    /**
     * Test mark notification as read
     * Validates: Requirements 15.3
     */
    @Test
    fun `markAsRead success returns unit`() = runTest {
        // Given: Mock successful response (204 No Content)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
        
        // When: Mark a notification as read
        val result = notificationRepository.markAsRead(notificationId = 1L)
        
        // Then: Should return success
        assertTrue(result is Result.Success)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("PUT", request.method)
        assertTrue(request.path?.contains("notifications/1/read") == true)
    }
    
    /**
     * Test mark notification as read with non-existent notification
     * Validates: Requirements 15.3
     */
    @Test
    fun `markAsRead with non-existent notification returns not found error`() = runTest {
        // Given: Mock 404 Not Found response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 404,
                "error": "Not Found",
                "message": "Notification not found",
                "path": "/api/notifications/999/read"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Mark non-existent notification as read
        val result = notificationRepository.markAsRead(notificationId = 999L)
        
        // Then: Should return not found error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.NotFoundException)
        assertEquals("Notification not found", error.message)
    }
    
    /**
     * Test mark all notifications as read
     * Validates: Requirements 15.4
     */
    @Test
    fun `markAllAsRead success returns unit`() = runTest {
        // Given: Mock successful response (204 No Content)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
        
        // When: Mark all notifications as read
        val result = notificationRepository.markAllAsRead()
        
        // Then: Should return success
        assertTrue(result is Result.Success)
        
        // Verify request was made
        val request = mockWebServer.takeRequest()
        assertEquals("PUT", request.method)
        assertTrue(request.path?.contains("notifications/read-all") == true)
    }
    
    /**
     * Test notification type parsing for LIKE
     * Validates: Requirements 15.5
     */
    @Test
    fun `getNotifications correctly parses LIKE notification type`() = runTest {
        // Given: Mock response with LIKE notification
        val responseJson = """
            {
                "content": [
                    {
                        "id": 1,
                        "userId": 123,
                        "type": "LIKE",
                        "actorId": 456,
                        "actorUsername": "john_doe",
                        "actorAvatarUrl": null,
                        "postId": 100,
                        "message": "john_doe liked your post",
                        "isRead": false,
                        "createdAt": "2024-01-15T10:30:00.000Z"
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
        
        // When: Get notifications
        val result = notificationRepository.getNotifications(page = 0, size = 10)
        
        // Then: Should correctly parse LIKE type
        assertTrue(result is Result.Success)
        val notification = (result as Result.Success).data.items[0]
        assertEquals(NotificationType.LIKE, notification.type)
        assertNotNull(notification.postId) // LIKE notifications have postId
    }
    
    /**
     * Test notification type parsing for COMMENT
     * Validates: Requirements 15.5
     */
    @Test
    fun `getNotifications correctly parses COMMENT notification type`() = runTest {
        // Given: Mock response with COMMENT notification
        val responseJson = """
            {
                "content": [
                    {
                        "id": 2,
                        "userId": 123,
                        "type": "COMMENT",
                        "actorId": 789,
                        "actorUsername": "jane_smith",
                        "actorAvatarUrl": null,
                        "postId": 100,
                        "message": "jane_smith commented on your post",
                        "isRead": false,
                        "createdAt": "2024-01-15T09:30:00.000Z"
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
        
        // When: Get notifications
        val result = notificationRepository.getNotifications(page = 0, size = 10)
        
        // Then: Should correctly parse COMMENT type
        assertTrue(result is Result.Success)
        val notification = (result as Result.Success).data.items[0]
        assertEquals(NotificationType.COMMENT, notification.type)
        assertNotNull(notification.postId) // COMMENT notifications have postId
    }
    
    /**
     * Test notification type parsing for FOLLOW
     * Validates: Requirements 15.5
     */
    @Test
    fun `getNotifications correctly parses FOLLOW notification type`() = runTest {
        // Given: Mock response with FOLLOW notification
        val responseJson = """
            {
                "content": [
                    {
                        "id": 3,
                        "userId": 123,
                        "type": "FOLLOW",
                        "actorId": 999,
                        "actorUsername": "bob_wilson",
                        "actorAvatarUrl": "https://example.com/avatar3.jpg",
                        "postId": null,
                        "message": "bob_wilson started following you",
                        "isRead": false,
                        "createdAt": "2024-01-15T08:30:00.000Z"
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
        
        // When: Get notifications
        val result = notificationRepository.getNotifications(page = 0, size = 10)
        
        // Then: Should correctly parse FOLLOW type
        assertTrue(result is Result.Success)
        val notification = (result as Result.Success).data.items[0]
        assertEquals(NotificationType.FOLLOW, notification.type)
        assertNull(notification.postId) // FOLLOW notifications don't have postId
    }
    
    /**
     * Test authorization error when accessing someone else's notifications
     * Validates: Requirements 15.1
     */
    @Test
    fun `getNotifications with unauthorized access returns authorization error`() = runTest {
        // Given: Mock 403 Forbidden response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 403,
                "error": "Forbidden",
                "message": "You don't have permission to access these notifications",
                "path": "/api/notifications"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(403)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Try to access notifications without proper authorization
        val result = notificationRepository.getNotifications(page = 0, size = 10)
        
        // Then: Should return authorization error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.AuthorizationException)
        assertEquals("You don't have permission to perform this action", error.message)
    }
    
    /**
     * Test authentication error
     * Validates: Requirements 15.1
     */
    @Test
    fun `getNotifications without authentication returns authentication error`() = runTest {
        // Given: Mock 401 Unauthorized response
        val errorJson = """
            {
                "timestamp": "2024-01-15T10:30:00.000Z",
                "status": 401,
                "error": "Unauthorized",
                "message": "Authentication required",
                "path": "/api/notifications"
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json")
        )
        
        // When: Get notifications without authentication
        val result = notificationRepository.getNotifications(page = 0, size = 10)
        
        // Then: Should return authentication error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.AuthenticationException)
        assertEquals("Authentication required", error.message)
    }
    
    /**
     * Test server error handling
     * Validates: Requirements 15.1
     */
    @Test
    fun `getNotifications with server error returns server exception`() = runTest {
        // Given: Mock 500 Internal Server Error response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )
        
        // When: Get notifications when server has error
        val result = notificationRepository.getNotifications(page = 0, size = 10)
        
        // Then: Should return server error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.ServerException)
        assertEquals("Server error. Please try again later.", error.message)
    }
    
    /**
     * Test network timeout error
     * Validates: Requirements 15.1
     */
    @Test
    fun `getNotifications with network timeout returns network exception`() = runTest {
        // Given: Mock server that doesn't respond (will timeout)
        // Don't enqueue any response - let it timeout
        
        // When: Get notifications with timeout
        val result = notificationRepository.getNotifications(page = 0, size = 10)
        
        // Then: Should return network error
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppException.NetworkException)
        assertTrue(error.message.contains("timed out") || error.message.contains("connection"))
    }
}
