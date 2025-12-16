package com.example.agrohub.data.mapper

import com.example.agrohub.data.remote.dto.*
import com.example.agrohub.domain.model.NotificationType
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for mapper functions.
 * Tests specific examples and edge cases for DTO to domain model conversion.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class MapperUnitTest {
    
    // ========== UserMapper Tests ==========
    
    @Test
    fun `UserMapper converts DTO with all fields to domain model`() = runTest {
        val dto = UserProfileDto(
            id = 123L,
            email = "test@example.com",
            username = "testuser",
            name = "Test User",
            bio = "Test bio",
            avatarUrl = "https://example.com/avatar.jpg",
            location = "Test City",
            website = "https://example.com",
            createdAt = "2024-01-01T00:00:00.000Z",
            updatedAt = "2024-01-02T00:00:00.000Z"
        )
        
        val domain = UserMapper.toDomain(dto)
        
        domain.id shouldBe 123L
        domain.email shouldBe "test@example.com"
        domain.username shouldBe "testuser"
        domain.name shouldBe "Test User"
        domain.bio shouldBe "Test bio"
        domain.avatarUrl shouldBe "https://example.com/avatar.jpg"
        domain.location shouldBe "Test City"
        domain.website shouldBe "https://example.com"
        domain.createdAt shouldNotBe null
        domain.updatedAt shouldNotBe null
    }
    
    @Test
    fun `UserMapper handles null optional fields with defaults`() = runTest {
        val dto = UserProfileDto(
            id = 123L,
            email = "test@example.com",
            username = "testuser",
            name = null,
            bio = null,
            avatarUrl = null,
            location = null,
            website = null,
            createdAt = "2024-01-01T00:00:00.000Z",
            updatedAt = null
        )
        
        val domain = UserMapper.toDomain(dto)
        
        domain.name shouldBe ""
        domain.bio shouldBe ""
        domain.avatarUrl shouldBe null
        domain.location shouldBe ""
        domain.website shouldBe null
        domain.updatedAt shouldBe null
    }
    
    @Test
    fun `UserMapper converts list of DTOs`() = runTest {
        val dtos = listOf(
            UserProfileDto(
                id = 1L,
                email = "user1@example.com",
                username = "user1",
                name = null,
                bio = null,
                avatarUrl = null,
                location = null,
                website = null,
                createdAt = "2024-01-01T00:00:00.000Z",
                updatedAt = null
            ),
            UserProfileDto(
                id = 2L,
                email = "user2@example.com",
                username = "user2",
                name = null,
                bio = null,
                avatarUrl = null,
                location = null,
                website = null,
                createdAt = "2024-01-01T00:00:00.000Z",
                updatedAt = null
            )
        )
        
        val domains = UserMapper.toDomainList(dtos)
        
        domains.size shouldBe 2
        domains[0].id shouldBe 1L
        domains[1].id shouldBe 2L
    }
    
    // ========== PostMapper Tests ==========
    
    @Test
    fun `PostMapper converts PostDto to domain model`() = runTest {
        val dto = PostDto(
            id = 456L,
            userId = 123L,
            content = "Test post content",
            mediaUrl = "https://example.com/image.jpg",
            createdAt = "2024-01-01T00:00:00.000Z",
            updatedAt = "2024-01-02T00:00:00.000Z"
        )
        
        val domain = PostMapper.toDomain(dto)
        
        domain.id shouldBe 456L
        domain.userId shouldBe 123L
        domain.content shouldBe "Test post content"
        domain.mediaUrl shouldBe "https://example.com/image.jpg"
        domain.createdAt shouldNotBe null
        domain.updatedAt shouldNotBe null
    }
    
    @Test
    fun `PostMapper handles null optional fields`() = runTest {
        val dto = PostDto(
            id = 456L,
            userId = 123L,
            content = "Test post content",
            mediaUrl = null,
            createdAt = "2024-01-01T00:00:00.000Z",
            updatedAt = null
        )
        
        val domain = PostMapper.toDomain(dto)
        
        domain.mediaUrl shouldBe null
        domain.updatedAt shouldBe null
    }
    
    @Test
    fun `PostMapper converts FeedPostDto to FeedPost domain model`() = runTest {
        val dto = FeedPostDto(
            id = 456L,
            userId = 123L,
            username = "testuser",
            userAvatarUrl = "https://example.com/avatar.jpg",
            content = "Test feed post",
            mediaUrl = "https://example.com/image.jpg",
            likeCount = 42,
            commentCount = 10,
            likedByCurrentUser = true,
            createdAt = "2024-01-01T00:00:00.000Z",
            updatedAt = null
        )
        
        val domain = PostMapper.feedPostToDomain(dto)
        
        domain.id shouldBe 456L
        domain.author.id shouldBe 123L
        domain.author.username shouldBe "testuser"
        domain.author.avatarUrl shouldBe "https://example.com/avatar.jpg"
        domain.content shouldBe "Test feed post"
        domain.mediaUrl shouldBe "https://example.com/image.jpg"
        domain.likeCount shouldBe 42
        domain.commentCount shouldBe 10
        domain.isLikedByCurrentUser shouldBe true
        domain.createdAt shouldNotBe null
    }
    
    // ========== CommentMapper Tests ==========
    
    @Test
    fun `CommentMapper converts CommentDto to domain model`() = runTest {
        val dto = CommentDto(
            id = 789L,
            postId = 456L,
            userId = 123L,
            username = "commenter",
            userAvatarUrl = "https://example.com/avatar.jpg",
            content = "Test comment",
            createdAt = "2024-01-01T00:00:00.000Z",
            updatedAt = null
        )
        
        val domain = CommentMapper.toDomain(dto)
        
        domain.id shouldBe 789L
        domain.postId shouldBe 456L
        domain.author.id shouldBe 123L
        domain.author.username shouldBe "commenter"
        domain.author.avatarUrl shouldBe "https://example.com/avatar.jpg"
        domain.content shouldBe "Test comment"
        domain.createdAt shouldNotBe null
        domain.updatedAt shouldBe null
    }
    
    @Test
    fun `CommentMapper converts list of DTOs`() = runTest {
        val dtos = listOf(
            CommentDto(
                id = 1L,
                postId = 456L,
                userId = 123L,
                username = "user1",
                userAvatarUrl = null,
                content = "Comment 1",
                createdAt = "2024-01-01T00:00:00.000Z",
                updatedAt = null
            ),
            CommentDto(
                id = 2L,
                postId = 456L,
                userId = 124L,
                username = "user2",
                userAvatarUrl = null,
                content = "Comment 2",
                createdAt = "2024-01-01T00:00:00.000Z",
                updatedAt = null
            )
        )
        
        val domains = CommentMapper.toDomainList(dtos)
        
        domains.size shouldBe 2
        domains[0].id shouldBe 1L
        domains[1].id shouldBe 2L
    }
    
    // ========== NotificationMapper Tests ==========
    
    @Test
    fun `NotificationMapper converts LIKE notification`() = runTest {
        val dto = NotificationDto(
            id = 999L,
            userId = 123L,
            type = "LIKE",
            actorId = 456L,
            actorUsername = "liker",
            actorAvatarUrl = "https://example.com/avatar.jpg",
            postId = 789L,
            message = "liker liked your post",
            isRead = false,
            createdAt = "2024-01-01T00:00:00.000Z"
        )
        
        val domain = NotificationMapper.toDomain(dto)
        
        domain.id shouldBe 999L
        domain.type shouldBe NotificationType.LIKE
        domain.actor.id shouldBe 456L
        domain.actor.username shouldBe "liker"
        domain.actor.avatarUrl shouldBe "https://example.com/avatar.jpg"
        domain.postId shouldBe 789L
        domain.message shouldBe "liker liked your post"
        domain.isRead shouldBe false
        domain.createdAt shouldNotBe null
    }
    
    @Test
    fun `NotificationMapper converts COMMENT notification`() = runTest {
        val dto = NotificationDto(
            id = 999L,
            userId = 123L,
            type = "COMMENT",
            actorId = 456L,
            actorUsername = "commenter",
            actorAvatarUrl = null,
            postId = 789L,
            message = "commenter commented on your post",
            isRead = true,
            createdAt = "2024-01-01T00:00:00.000Z"
        )
        
        val domain = NotificationMapper.toDomain(dto)
        
        domain.type shouldBe NotificationType.COMMENT
        domain.isRead shouldBe true
    }
    
    @Test
    fun `NotificationMapper converts FOLLOW notification`() = runTest {
        val dto = NotificationDto(
            id = 999L,
            userId = 123L,
            type = "FOLLOW",
            actorId = 456L,
            actorUsername = "follower",
            actorAvatarUrl = null,
            postId = null,
            message = "follower started following you",
            isRead = false,
            createdAt = "2024-01-01T00:00:00.000Z"
        )
        
        val domain = NotificationMapper.toDomain(dto)
        
        domain.type shouldBe NotificationType.FOLLOW
        domain.postId shouldBe null
    }
    
    // ========== PagedDataMapper Tests ==========
    
    @Test
    fun `PagedDataMapper converts paged user response`() = runTest {
        val dto = PagedResponseDto(
            content = listOf(
                UserProfileDto(
                    id = 1L,
                    email = "user1@example.com",
                    username = "user1",
                    name = null,
                    bio = null,
                    avatarUrl = null,
                    location = null,
                    website = null,
                    createdAt = "2024-01-01T00:00:00.000Z",
                    updatedAt = null
                )
            ),
            pageable = PageableDto(
                pageNumber = 0,
                pageSize = 10
            ),
            totalElements = 100,
            totalPages = 10,
            last = false
        )
        
        val domain = PagedDataMapper.toUserPagedData(dto)
        
        domain.items.size shouldBe 1
        domain.currentPage shouldBe 0
        domain.pageSize shouldBe 10
        domain.totalElements shouldBe 100
        domain.totalPages shouldBe 10
        domain.isLastPage shouldBe false
    }
    
    @Test
    fun `PagedDataMapper handles last page`() = runTest {
        val dto = PagedResponseDto(
            content = listOf<UserProfileDto>(),
            pageable = PageableDto(
                pageNumber = 9,
                pageSize = 10
            ),
            totalElements = 100,
            totalPages = 10,
            last = true
        )
        
        val domain = PagedDataMapper.toUserPagedData(dto)
        
        domain.isLastPage shouldBe true
        domain.items.size shouldBe 0
    }
    
    @Test
    fun `PagedDataMapper converts paged post response`() = runTest {
        val dto = PagedResponseDto(
            content = listOf(
                PostDto(
                    id = 1L,
                    userId = 123L,
                    content = "Post 1",
                    mediaUrl = null,
                    createdAt = "2024-01-01T00:00:00.000Z",
                    updatedAt = null
                )
            ),
            pageable = PageableDto(
                pageNumber = 0,
                pageSize = 10
            ),
            totalElements = 50,
            totalPages = 5,
            last = false
        )
        
        val domain = PagedDataMapper.toPostPagedData(dto)
        
        domain.items.size shouldBe 1
        domain.items[0].id shouldBe 1L
    }
    
    // ========== DateTimeUtils Tests ==========
    
    @Test
    fun `DateTimeUtils parses ISO 8601 with timezone`() = runTest {
        val dateString = "2024-01-15T14:30:00.000Z"
        val dateTime = DateTimeUtils.parseDateTime(dateString)
        
        dateTime shouldNotBe null
        dateTime.year shouldBe 2024
        dateTime.monthValue shouldBe 1
        dateTime.dayOfMonth shouldBe 15
    }
    
    @Test
    fun `DateTimeUtils parses ISO 8601 without timezone`() = runTest {
        val dateString = "2024-01-15T14:30:00"
        val dateTime = DateTimeUtils.parseDateTime(dateString)
        
        dateTime shouldNotBe null
        dateTime.year shouldBe 2024
    }
    
    @Test
    fun `DateTimeUtils parseDateTimeOrNull returns null for invalid string`() = runTest {
        val result = DateTimeUtils.parseDateTimeOrNull("invalid-date")
        
        result shouldBe null
    }
    
    @Test
    fun `DateTimeUtils parseDateTimeOrNull returns null for null input`() = runTest {
        val result = DateTimeUtils.parseDateTimeOrNull(null)
        
        result shouldBe null
    }
}
