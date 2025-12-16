package com.example.agrohub.data.mapper

import com.example.agrohub.data.remote.dto.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.RandomSource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

/**
 * Property-based tests for mapper functions.
 * Tests universal properties that should hold across all valid DTO inputs.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class MapperPropertiesTest {
    
    private lateinit var rs: RandomSource
    
    @Before
    fun setup() {
        rs = RandomSource.seeded(Random.nextLong())
    }
    
    /**
     * Feature: backend-api-integration, Property 11: DTO to Domain Mapping
     * Validates: Requirements 17.1
     * 
     * For any valid DTO object, mapping to a domain model should produce a valid domain object
     * with all required fields populated.
     */
    @Test
    fun `property 11 - user profile DTO to domain mapping produces valid domain objects`() = runTest {
        repeat(100) {
            val dto = arbUserProfileDto().sample(rs).value
            val domain = UserMapper.toDomain(dto)
            
            // Verify all required fields are populated
            domain.id shouldBe dto.id
            domain.email shouldBe dto.email
            domain.username shouldBe dto.username
            domain.name shouldNotBe null
            domain.bio shouldNotBe null
            domain.location shouldNotBe null
            domain.createdAt shouldNotBe null
        }
    }
    
    @Test
    fun `property 11 - post DTO to domain mapping produces valid domain objects`() = runTest {
        repeat(100) {
            val dto = arbPostDto().sample(rs).value
            val domain = PostMapper.toDomain(dto)
            
            // Verify all required fields are populated
            domain.id shouldBe dto.id
            domain.userId shouldBe dto.userId
            domain.content shouldBe dto.content
            domain.createdAt shouldNotBe null
        }
    }
    
    @Test
    fun `property 11 - feed post DTO to domain mapping produces valid domain objects`() = runTest {
        repeat(100) {
            val dto = arbFeedPostDto().sample(rs).value
            val domain = PostMapper.feedPostToDomain(dto)
            
            // Verify all required fields are populated
            domain.id shouldBe dto.id
            domain.author shouldNotBe null
            domain.author.id shouldBe dto.userId
            domain.author.username shouldBe dto.username
            domain.content shouldBe dto.content
            domain.likeCount shouldBe dto.likeCount
            domain.commentCount shouldBe dto.commentCount
            domain.isLikedByCurrentUser shouldBe dto.likedByCurrentUser
            domain.createdAt shouldNotBe null
        }
    }
    
    @Test
    fun `property 11 - comment DTO to domain mapping produces valid domain objects`() = runTest {
        repeat(100) {
            val dto = arbCommentDto().sample(rs).value
            val domain = CommentMapper.toDomain(dto)
            
            // Verify all required fields are populated
            domain.id shouldBe dto.id
            domain.postId shouldBe dto.postId
            domain.author shouldNotBe null
            domain.author.id shouldBe dto.userId
            domain.author.username shouldBe dto.username
            domain.content shouldBe dto.content
            domain.createdAt shouldNotBe null
        }
    }
    
    @Test
    fun `property 11 - notification DTO to domain mapping produces valid domain objects`() = runTest {
        repeat(100) {
            val dto = arbNotificationDto().sample(rs).value
            val domain = NotificationMapper.toDomain(dto)
            
            // Verify all required fields are populated
            domain.id shouldBe dto.id
            domain.type shouldNotBe null
            domain.actor shouldNotBe null
            domain.actor.id shouldBe dto.actorId
            domain.actor.username shouldBe dto.actorUsername
            domain.message shouldBe dto.message
            domain.isRead shouldBe dto.isRead
            domain.createdAt shouldNotBe null
        }
    }
    
    @Test
    fun `property 11 - paged data mapping preserves pagination metadata`() = runTest {
        repeat(100) {
            val dto = arbPagedResponseDto(arbUserProfileDto()).sample(rs).value
            val domain = PagedDataMapper.toUserPagedData(dto)
            
            // Verify pagination metadata is preserved
            domain.currentPage shouldBe dto.pageable.pageNumber
            domain.pageSize shouldBe dto.pageable.pageSize
            domain.totalElements shouldBe dto.totalElements
            domain.totalPages shouldBe dto.totalPages
            domain.isLastPage shouldBe dto.last
            domain.items.size shouldBe dto.content.size
        }
    }
}

// Arbitrary generators for DTOs

private fun arbUserProfileDto(): Arb<UserProfileDto> = arbitrary {
    UserProfileDto(
        id = Arb.long(1L..1000000L).bind(),
        email = Arb.email().bind(),
        username = Arb.string(3..20, Codepoint.alphanumeric()).bind(),
        name = Arb.string(1..50).orNull().bind(),
        bio = Arb.string(0..200).orNull().bind(),
        avatarUrl = Arb.string(10..100).orNull().bind(),
        location = Arb.string(0..100).orNull().bind(),
        website = Arb.string(10..100).orNull().bind(),
        createdAt = arbIsoDateTime().bind(),
        updatedAt = arbIsoDateTime().orNull().bind()
    )
}

private fun arbPostDto(): Arb<PostDto> = arbitrary {
    PostDto(
        id = Arb.long(1L..1000000L).bind(),
        userId = Arb.long(1L..1000000L).bind(),
        content = Arb.string(1..500).bind(),
        mediaUrl = Arb.string(10..100).orNull().bind(),
        createdAt = arbIsoDateTime().bind(),
        updatedAt = arbIsoDateTime().orNull().bind()
    )
}

private fun arbFeedPostDto(): Arb<FeedPostDto> = arbitrary {
    FeedPostDto(
        id = Arb.long(1L..1000000L).bind(),
        userId = Arb.long(1L..1000000L).bind(),
        username = Arb.string(3..20, Codepoint.alphanumeric()).bind(),
        userAvatarUrl = Arb.string(10..100).orNull().bind(),
        content = Arb.string(1..500).bind(),
        mediaUrl = Arb.string(10..100).orNull().bind(),
        likeCount = Arb.int(0..10000).bind(),
        commentCount = Arb.int(0..1000).bind(),
        likedByCurrentUser = Arb.boolean().bind(),
        createdAt = arbIsoDateTime().bind(),
        updatedAt = arbIsoDateTime().orNull().bind()
    )
}

private fun arbCommentDto(): Arb<CommentDto> = arbitrary {
    CommentDto(
        id = Arb.long(1L..1000000L).bind(),
        postId = Arb.long(1L..1000000L).bind(),
        userId = Arb.long(1L..1000000L).bind(),
        username = Arb.string(3..20, Codepoint.alphanumeric()).bind(),
        userAvatarUrl = Arb.string(10..100).orNull().bind(),
        content = Arb.string(1..500).bind(),
        createdAt = arbIsoDateTime().bind(),
        updatedAt = arbIsoDateTime().orNull().bind()
    )
}

private fun arbNotificationDto(): Arb<NotificationDto> = arbitrary {
    NotificationDto(
        id = Arb.long(1L..1000000L).bind(),
        userId = Arb.long(1L..1000000L).bind(),
        type = Arb.of("LIKE", "COMMENT", "FOLLOW").bind(),
        actorId = Arb.long(1L..1000000L).bind(),
        actorUsername = Arb.string(3..20, Codepoint.alphanumeric()).bind(),
        actorAvatarUrl = Arb.string(10..100).orNull().bind(),
        postId = Arb.long(1L..1000000L).orNull().bind(),
        message = Arb.string(10..200).bind(),
        isRead = Arb.boolean().bind(),
        createdAt = arbIsoDateTime().bind()
    )
}

private fun <T> arbPagedResponseDto(contentArb: Arb<T>): Arb<PagedResponseDto<T>> = arbitrary {
    val pageNumber = Arb.int(0..10).bind()
    val pageSize = Arb.int(1..50).bind()
    val totalElements = Arb.int(0..1000).bind()
    val totalPages = if (totalElements == 0) 0 else (totalElements + pageSize - 1) / pageSize
    val isLast = pageNumber >= totalPages - 1
    
    PagedResponseDto(
        content = Arb.list(contentArb, 0..pageSize).bind(),
        pageable = PageableDto(
            pageNumber = pageNumber,
            pageSize = pageSize
        ),
        totalElements = totalElements,
        totalPages = totalPages,
        last = isLast
    )
}

/**
 * Generates valid ISO 8601 formatted date/time strings.
 */
private fun arbIsoDateTime(): Arb<String> = arbitrary {
    val year = Arb.int(2020..2024).bind()
    val month = Arb.int(1..12).bind()
    val day = Arb.int(1..28).bind() // Use 28 to avoid invalid dates
    val hour = Arb.int(0..23).bind()
    val minute = Arb.int(0..59).bind()
    val second = Arb.int(0..59).bind()
    
    String.format("%04d-%02d-%02dT%02d:%02d:%02d.000Z", year, month, day, hour, minute, second)
}
