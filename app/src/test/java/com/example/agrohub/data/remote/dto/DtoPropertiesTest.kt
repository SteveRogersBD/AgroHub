package com.example.agrohub.data.remote.dto

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
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
 * Property-based tests for DTO JSON deserialization, optional field handling, and timestamp parsing.
 * 
 * Feature: backend-api-integration
 * - Property 8: DTO JSON Deserialization (Validates: Requirements 16.1)
 * - Property 9: Optional Field Null Handling (Validates: Requirements 16.3)
 * - Property 10: ISO 8601 Timestamp Parsing (Validates: Requirements 16.4)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class DtoPropertiesTest {

    private lateinit var moshi: Moshi
    private lateinit var rs: RandomSource

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        rs = RandomSource.seeded(Random.nextLong())
    }

    // ========== Property 8: DTO JSON Deserialization ==========

    /**
     * Feature: backend-api-integration, Property 8: DTO JSON Deserialization
     * Validates: Requirements 16.1
     */
    @Test
    fun `property 8 - login response DTO deserializes from valid JSON`() = runTest {
        val accessTokenGen = Arb.string(10..100)
        val refreshTokenGen = Arb.string(10..100)
        val tokenTypeGen = Arb.string(5..20)
        val expiresInGen = Arb.long(3600L..86400L)
        val userIdGen = Arb.long(1L..1000000L)
        val usernameGen = Arb.string(3..50)
        val emailGen = Arb.email()

        repeat(100) {
            val accessToken = accessTokenGen.sample(rs).value
            val refreshToken = refreshTokenGen.sample(rs).value
            val tokenType = tokenTypeGen.sample(rs).value
            val expiresIn = expiresInGen.sample(rs).value
            val userId = userIdGen.sample(rs).value
            val username = usernameGen.sample(rs).value
            val email = emailGen.sample(rs).value

            val json = """
                {
                    "accessToken": "$accessToken",
                    "refreshToken": "$refreshToken",
                    "tokenType": "$tokenType",
                    "expiresIn": $expiresIn,
                    "userId": $userId,
                    "username": "$username",
                    "email": "$email"
                }
            """.trimIndent()

            val adapter = moshi.adapter(LoginResponseDto::class.java)
            val dto = adapter.fromJson(json)

            dto shouldNotBe null
            dto!!.accessToken shouldBe accessToken
            dto.refreshToken shouldBe refreshToken
            dto.tokenType shouldBe tokenType
            dto.expiresIn shouldBe expiresIn
            dto.userId shouldBe userId
            dto.username shouldBe username
            dto.email shouldBe email
        }
    }

    @Test
    fun `property 8 - user profile DTO deserializes from valid JSON`() = runTest {
        repeat(100) {
            val id = Arb.long(1L..1000000L).sample(rs).value
            val email = Arb.email().sample(rs).value
            val username = Arb.string(3..50).sample(rs).value
            val name = Arb.string(1..100).sample(rs).value
            val bio = Arb.string(1..500).sample(rs).value
            val avatarUrl = Arb.string(10..200).sample(rs).value
            val location = Arb.string(1..100).sample(rs).value
            val website = Arb.string(10..200).sample(rs).value
            val createdAt = Arb.isoDateTime().sample(rs).value
            val updatedAt = Arb.isoDateTime().sample(rs).value

            val json = """
                {
                    "id": $id,
                    "email": "$email",
                    "username": "$username",
                    "name": "$name",
                    "bio": "$bio",
                    "avatarUrl": "$avatarUrl",
                    "location": "$location",
                    "website": "$website",
                    "createdAt": "$createdAt",
                    "updatedAt": "$updatedAt"
                }
            """.trimIndent()

            val adapter = moshi.adapter(UserProfileDto::class.java)
            val dto = adapter.fromJson(json)

            dto shouldNotBe null
            dto!!.id shouldBe id
            dto.email shouldBe email
            dto.username shouldBe username
            dto.name shouldBe name
            dto.bio shouldBe bio
        }
    }

    @Test
    fun `property 8 - post DTO deserializes from valid JSON`() = runTest {
        repeat(100) {
            val id = Arb.long(1L..1000000L).sample(rs).value
            val userId = Arb.long(1L..1000000L).sample(rs).value
            val content = Arb.string(1..1000).sample(rs).value.replace("\"", "'")
            val createdAt = Arb.isoDateTime().sample(rs).value

            val json = """
                {
                    "id": $id,
                    "userId": $userId,
                    "content": "$content",
                    "createdAt": "$createdAt"
                }
            """.trimIndent()

            val adapter = moshi.adapter(PostDto::class.java)
            val dto = adapter.fromJson(json)

            dto shouldNotBe null
            dto!!.id shouldBe id
            dto.userId shouldBe userId
            dto.content shouldBe content
        }
    }

    // ========== Property 9: Optional Field Null Handling ==========

    /**
     * Feature: backend-api-integration, Property 9: Optional Field Null Handling
     * Validates: Requirements 16.3
     */
    @Test
    fun `property 9 - user profile DTO handles missing optional fields`() = runTest {
        repeat(100) {
            val id = Arb.long(1L..1000000L).sample(rs).value
            val email = Arb.email().sample(rs).value
            val username = Arb.string(3..50).sample(rs).value
            val createdAt = Arb.isoDateTime().sample(rs).value

            // JSON with only required fields
            val json = """
                {
                    "id": $id,
                    "email": "$email",
                    "username": "$username",
                    "createdAt": "$createdAt"
                }
            """.trimIndent()

            val adapter = moshi.adapter(UserProfileDto::class.java)
            val dto = adapter.fromJson(json)

            dto shouldNotBe null
            dto!!.id shouldBe id
            dto.email shouldBe email
            dto.username shouldBe username
            dto.createdAt shouldBe createdAt
            
            // Verify optional fields are null
            dto.name shouldBe null
            dto.bio shouldBe null
            dto.avatarUrl shouldBe null
            dto.location shouldBe null
            dto.website shouldBe null
            dto.updatedAt shouldBe null
        }
    }

    @Test
    fun `property 9 - post DTO handles missing optional fields`() = runTest {
        repeat(100) {
            val id = Arb.long(1L..1000000L).sample(rs).value
            val userId = Arb.long(1L..1000000L).sample(rs).value
            val content = Arb.string(1..1000).sample(rs).value.replace("\"", "'")
            val createdAt = Arb.isoDateTime().sample(rs).value

            val json = """
                {
                    "id": $id,
                    "userId": $userId,
                    "content": "$content",
                    "createdAt": "$createdAt"
                }
            """.trimIndent()

            val adapter = moshi.adapter(PostDto::class.java)
            val dto = adapter.fromJson(json)

            dto shouldNotBe null
            dto!!.mediaUrl shouldBe null
            dto.updatedAt shouldBe null
        }
    }

    @Test
    fun `property 9 - feed post DTO handles missing optional fields`() = runTest {
        repeat(100) {
            val id = Arb.long(1L..1000000L).sample(rs).value
            val userId = Arb.long(1L..1000000L).sample(rs).value
            val username = Arb.string(3..50).sample(rs).value
            val content = Arb.string(1..1000).sample(rs).value.replace("\"", "'")
            val likeCount = Arb.int(0..10000).sample(rs).value
            val commentCount = Arb.int(0..1000).sample(rs).value
            val likedByCurrentUser = Arb.boolean().sample(rs).value
            val createdAt = Arb.isoDateTime().sample(rs).value

            val json = """
                {
                    "id": $id,
                    "userId": $userId,
                    "username": "$username",
                    "content": "$content",
                    "likeCount": $likeCount,
                    "commentCount": $commentCount,
                    "likedByCurrentUser": $likedByCurrentUser,
                    "createdAt": "$createdAt"
                }
            """.trimIndent()

            val adapter = moshi.adapter(FeedPostDto::class.java)
            val dto = adapter.fromJson(json)

            dto shouldNotBe null
            dto!!.userAvatarUrl shouldBe null
            dto.mediaUrl shouldBe null
            dto.updatedAt shouldBe null
        }
    }

    @Test
    fun `property 9 - create profile request DTO handles all fields as optional`() = runTest {
        repeat(100) {
            val json = "{}"

            val adapter = moshi.adapter(CreateProfileRequestDto::class.java)
            val dto = adapter.fromJson(json)

            dto shouldNotBe null
            dto!!.name shouldBe null
            dto.bio shouldBe null
            dto.avatarUrl shouldBe null
            dto.location shouldBe null
            dto.website shouldBe null
        }
    }

    // ========== Property 10: ISO 8601 Timestamp Parsing ==========

    /**
     * Feature: backend-api-integration, Property 10: ISO 8601 Timestamp Parsing
     * Validates: Requirements 16.4
     */
    @Test
    fun `property 10 - ISO 8601 timestamps are parsed correctly in user profile DTO`() = runTest {
        repeat(100) {
            val id = Arb.long(1L..1000000L).sample(rs).value
            val email = Arb.email().sample(rs).value
            val username = Arb.string(3..50).sample(rs).value
            val createdAt = Arb.isoDateTime().sample(rs).value
            val updatedAt = Arb.isoDateTime().sample(rs).value

            val json = """
                {
                    "id": $id,
                    "email": "$email",
                    "username": "$username",
                    "createdAt": "$createdAt",
                    "updatedAt": "$updatedAt"
                }
            """.trimIndent()

            val adapter = moshi.adapter(UserProfileDto::class.java)
            val dto = adapter.fromJson(json)

            dto shouldNotBe null
            dto!!.createdAt shouldBe createdAt
            dto.updatedAt shouldBe updatedAt
            
            // Verify ISO 8601 format is preserved
            dto.createdAt shouldMatch Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z""")
            dto.updatedAt!! shouldMatch Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z""")
        }
    }

    @Test
    fun `property 10 - ISO 8601 timestamps are parsed correctly in post DTO`() = runTest {
        repeat(100) {
            val id = Arb.long(1L..1000000L).sample(rs).value
            val userId = Arb.long(1L..1000000L).sample(rs).value
            val content = Arb.string(1..1000).sample(rs).value.replace("\"", "'")
            val createdAt = Arb.isoDateTime().sample(rs).value

            val json = """
                {
                    "id": $id,
                    "userId": $userId,
                    "content": "$content",
                    "createdAt": "$createdAt"
                }
            """.trimIndent()

            val adapter = moshi.adapter(PostDto::class.java)
            val dto = adapter.fromJson(json)

            dto shouldNotBe null
            dto!!.createdAt shouldBe createdAt
            dto.createdAt shouldMatch Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z""")
        }
    }

    @Test
    fun `property 10 - ISO 8601 timestamps are parsed correctly in notification DTO`() = runTest {
        repeat(100) {
            val id = Arb.long(1L..1000000L).sample(rs).value
            val userId = Arb.long(1L..1000000L).sample(rs).value
            val type = Arb.of("LIKE", "COMMENT", "FOLLOW").sample(rs).value
            val actorId = Arb.long(1L..1000000L).sample(rs).value
            val actorUsername = Arb.string(3..50).sample(rs).value
            val message = Arb.string(1..200).sample(rs).value
            val isRead = Arb.boolean().sample(rs).value
            val createdAt = Arb.isoDateTime().sample(rs).value

            val json = """
                {
                    "id": $id,
                    "userId": $userId,
                    "type": "$type",
                    "actorId": $actorId,
                    "actorUsername": "$actorUsername",
                    "message": "$message",
                    "isRead": $isRead,
                    "createdAt": "$createdAt"
                }
            """.trimIndent()

            val adapter = moshi.adapter(NotificationDto::class.java)
            val dto = adapter.fromJson(json)

            dto shouldNotBe null
            dto!!.createdAt shouldBe createdAt
            dto.createdAt shouldMatch Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z""")
        }
    }

    @Test
    fun `property 10 - register response DTO parses ISO 8601 timestamp`() = runTest {
        repeat(100) {
            val id = Arb.long(1L..1000000L).sample(rs).value
            val email = Arb.email().sample(rs).value
            val username = Arb.string(3..50).sample(rs).value
            val role = Arb.string(5..20).sample(rs).value
            val createdAt = Arb.isoDateTime().sample(rs).value

            val json = """
                {
                    "id": $id,
                    "email": "$email",
                    "username": "$username",
                    "role": "$role",
                    "createdAt": "$createdAt"
                }
            """.trimIndent()

            val adapter = moshi.adapter(RegisterResponseDto::class.java)
            val dto = adapter.fromJson(json)

            dto shouldNotBe null
            dto!!.createdAt shouldBe createdAt
            dto.createdAt shouldMatch Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z""")
        }
    }

    @Test
    fun `property 10 - error response DTO parses ISO 8601 timestamp`() = runTest {
        repeat(100) {
            val timestamp = Arb.isoDateTime().sample(rs).value
            val status = Arb.int(400..599).sample(rs).value
            val error = Arb.string(5..50).sample(rs).value
            val message = Arb.string(10..200).sample(rs).value
            val path = Arb.string(5..100).sample(rs).value

            val json = """
                {
                    "timestamp": "$timestamp",
                    "status": $status,
                    "error": "$error",
                    "message": "$message",
                    "path": "$path"
                }
            """.trimIndent()

            val adapter = moshi.adapter(ErrorResponseDto::class.java)
            val dto = adapter.fromJson(json)

            dto shouldNotBe null
            dto!!.timestamp shouldBe timestamp
            dto.timestamp shouldMatch Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z""")
        }
    }
}

// Helper extension to generate ISO 8601 datetime strings
private fun Arb.Companion.isoDateTime(): Arb<String> = arbitrary {
    val year = Arb.int(2020..2024).bind()
    val month = Arb.int(1..12).bind()
    val day = Arb.int(1..28).bind()
    val hour = Arb.int(0..23).bind()
    val minute = Arb.int(0..59).bind()
    val second = Arb.int(0..59).bind()
    String.format("%04d-%02d-%02dT%02d:%02d:%02d.000Z", year, month, day, hour, minute, second)
}

// Helper extension to generate email addresses
private fun Arb.Companion.email(): Arb<String> = arbitrary {
    val username = Arb.string(3..20, Codepoint.alphanumeric()).bind()
    val domain = Arb.string(3..20, Codepoint.alphanumeric()).bind()
    val tld = Arb.of("com", "org", "net", "io").bind()
    "$username@$domain.$tld"
}
