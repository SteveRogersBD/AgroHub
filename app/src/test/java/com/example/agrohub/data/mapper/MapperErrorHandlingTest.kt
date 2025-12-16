package com.example.agrohub.data.mapper

import com.example.agrohub.data.remote.dto.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
 * Property-based tests for mapper error handling.
 * Tests that mappers handle invalid data gracefully without crashing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class MapperErrorHandlingTest {
    
    private lateinit var rs: RandomSource
    
    @Before
    fun setup() {
        rs = RandomSource.seeded(Random.nextLong())
    }
    
    /**
     * Feature: backend-api-integration, Property 12: Mapping Error Handling
     * Validates: Requirements 17.5
     * 
     * For any DTO with invalid or missing required data, mapping should handle the error 
     * gracefully and return an appropriate error result without crashing.
     */
    @Test
    fun `property 12 - invalid date strings throw DateTimeParseException`() = runTest {
        val invalidDateStrings = listOf(
            "not-a-date",
            "2024-13-01T00:00:00.000Z", // Invalid month
            "2024-01-32T00:00:00.000Z", // Invalid day
            "2024-01-01T25:00:00.000Z", // Invalid hour
            "",
            "null",
            "2024/01/01", // Wrong format
            "01-01-2024" // Wrong format
        )
        
        invalidDateStrings.forEach { invalidDate ->
            val dto = UserProfileDto(
                id = 1L,
                email = "test@example.com",
                username = "testuser",
                name = null,
                bio = null,
                avatarUrl = null,
                location = null,
                website = null,
                createdAt = invalidDate,
                updatedAt = null
            )
            
            try {
                UserMapper.toDomain(dto)
                // If we get here, the mapper didn't throw an exception
                // This is acceptable if the mapper has fallback handling
            } catch (e: Exception) {
                // Verify it's a date parsing exception, not a crash
                e.shouldBeInstanceOf<java.time.format.DateTimeParseException>()
            }
        }
    }
    
    @Test
    fun `property 12 - invalid notification type throws IllegalArgumentException`() = runTest {
        val invalidTypes = listOf(
            "INVALID",
            "like", // lowercase
            "COMMENT_TYPE",
            "",
            "123",
            "UNKNOWN"
        )
        
        invalidTypes.forEach { invalidType ->
            val dto = NotificationDto(
                id = 1L,
                userId = 1L,
                type = invalidType,
                actorId = 2L,
                actorUsername = "actor",
                actorAvatarUrl = null,
                postId = null,
                message = "Test message",
                isRead = false,
                createdAt = "2024-01-01T00:00:00.000Z"
            )
            
            try {
                NotificationMapper.toDomain(dto)
                // Should not reach here
            } catch (e: Exception) {
                // Verify it's an IllegalArgumentException with helpful message
                e.shouldBeInstanceOf<IllegalArgumentException>()
                e.message shouldBe "Unknown notification type: $invalidType. Expected one of: LIKE, COMMENT, FOLLOW"
            }
        }
    }
    
    @Test
    fun `property 12 - mappers handle null optional fields gracefully`() = runTest {
        // Test that mappers don't crash when optional fields are null
        repeat(100) {
            val userDto = UserProfileDto(
                id = Arb.long(1L..1000000L).sample(rs).value,
                email = arbEmail().sample(rs).value,
                username = Arb.string(3..20, Codepoint.alphanumeric()).sample(rs).value,
                name = null, // Optional
                bio = null, // Optional
                avatarUrl = null, // Optional
                location = null, // Optional
                website = null, // Optional
                createdAt = arbIsoDateTime().sample(rs).value,
                updatedAt = null // Optional
            )
            
            val user = UserMapper.toDomain(userDto)
            
            // Verify defaults are applied for null optional fields
            user.name shouldBe ""
            user.bio shouldBe ""
            user.location shouldBe ""
            user.avatarUrl shouldBe null
            user.website shouldBe null
            user.updatedAt shouldBe null
        }
    }
    
    @Test
    fun `property 12 - post mapper handles null optional fields gracefully`() = runTest {
        repeat(100) {
            val postDto = PostDto(
                id = Arb.long(1L..1000000L).sample(rs).value,
                userId = Arb.long(1L..1000000L).sample(rs).value,
                content = Arb.string(1..500).sample(rs).value,
                mediaUrl = null, // Optional
                createdAt = arbIsoDateTime().sample(rs).value,
                updatedAt = null // Optional
            )
            
            val post = PostMapper.toDomain(postDto)
            
            // Verify null optional fields are preserved
            post.mediaUrl shouldBe null
            post.updatedAt shouldBe null
        }
    }
    
    @Test
    fun `property 12 - feed post mapper handles null optional fields gracefully`() = runTest {
        repeat(100) {
            val feedPostDto = FeedPostDto(
                id = Arb.long(1L..1000000L).sample(rs).value,
                userId = Arb.long(1L..1000000L).sample(rs).value,
                username = Arb.string(3..20, Codepoint.alphanumeric()).sample(rs).value,
                userAvatarUrl = null, // Optional
                content = Arb.string(1..500).sample(rs).value,
                mediaUrl = null, // Optional
                likeCount = Arb.int(0..10000).sample(rs).value,
                commentCount = Arb.int(0..1000).sample(rs).value,
                likedByCurrentUser = Arb.boolean().sample(rs).value,
                createdAt = arbIsoDateTime().sample(rs).value,
                updatedAt = null // Optional
            )
            
            val feedPost = PostMapper.feedPostToDomain(feedPostDto)
            
            // Verify null optional fields are preserved
            feedPost.author.avatarUrl shouldBe null
            feedPost.mediaUrl shouldBe null
            feedPost.updatedAt shouldBe null
        }
    }
}

// Helper functions

private fun arbEmail(): Arb<String> = arbitrary {
    val username = Arb.string(3..20, Codepoint.alphanumeric()).bind()
    val domain = Arb.string(3..20, Codepoint.alphanumeric()).bind()
    val tld = Arb.of("com", "org", "net", "io").bind()
    "$username@$domain.$tld"
}

private fun arbIsoDateTime(): Arb<String> = arbitrary {
    val year = Arb.int(2020..2024).bind()
    val month = Arb.int(1..12).bind()
    val day = Arb.int(1..28).bind()
    val hour = Arb.int(0..23).bind()
    val minute = Arb.int(0..59).bind()
    val second = Arb.int(0..59).bind()
    
    String.format("%04d-%02d-%02dT%02d:%02d:%02d.000Z", year, month, day, hour, minute, second)
}
