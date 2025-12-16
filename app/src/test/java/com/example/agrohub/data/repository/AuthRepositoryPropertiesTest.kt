package com.example.agrohub.data.repository

import com.example.agrohub.data.remote.api.AuthApiService
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import com.example.agrohub.security.TokenManager
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.*
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

/**
 * Property-based tests for AuthRepository implementation.
 * Tests universal properties that should hold across all valid inputs.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class AuthRepositoryPropertiesTest {
    
    private lateinit var authRepository: AuthRepositoryImpl
    private lateinit var authApiService: AuthApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var rs: RandomSource
    
    @Before
    fun setup() {
        authApiService = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)
        authRepository = AuthRepositoryImpl(authApiService, tokenManager)
        rs = RandomSource.seeded(Random.nextLong())
    }
    
    /**
     * Feature: backend-api-integration, Property 7: Email Validation Before API Call
     * Validates: Requirements 6.5
     * 
     * For any email string that does not match valid email format, the registration/login 
     * should fail locally without making an API request.
     */
    @Test
    fun `property 7 - invalid email format fails validation before API call`() = runTest {
        repeat(100) {
            val invalidEmail = arbInvalidEmail().sample(rs).value
            val username = Arb.string(5..20, Codepoint.alphanumeric()).sample(rs).value
            val password = Arb.string(8..20).sample(rs).value
            
            // Test register with invalid email
            val registerResult = authRepository.register(invalidEmail, username, password)
            
            // Should return validation error without making API call
            registerResult.shouldBeInstanceOf<Result.Error>()
            val registerError = (registerResult as Result.Error).exception
            registerError.shouldBeInstanceOf<AppException.ValidationException>()
            registerError.message shouldBe "Invalid email format"
            
            // Test login with invalid email
            val loginResult = authRepository.login(invalidEmail, password)
            
            // Should return validation error without making API call
            loginResult.shouldBeInstanceOf<Result.Error>()
            val loginError = (loginResult as Result.Error).exception
            loginError.shouldBeInstanceOf<AppException.ValidationException>()
            loginError.message shouldBe "Invalid email format"
        }
    }
}

/**
 * Generator for invalid email strings.
 * Produces various forms of invalid email addresses.
 */
private fun arbInvalidEmail(): Arb<String> = arbitrary {
    Arb.choice(
        // Empty or blank strings
        Arb.constant(""),
        Arb.constant("   "),
        // Strings without @ symbol
        Arb.string(1..20, Codepoint.alphanumeric()),
        // Strings with @ but no domain
        arbitrary { Arb.string(1..10, Codepoint.alphanumeric()).bind() + "@" },
        // Strings with @ but no local part
        arbitrary { "@" + Arb.string(1..10, Codepoint.alphanumeric()).bind() },
        // Strings with multiple @ symbols
        arbitrary { 
            val s = Arb.string(1..10, Codepoint.alphanumeric()).bind()
            "${s}@@${s}.com"
        },
        // Strings with spaces
        arbitrary { 
            val s = Arb.string(1..10, Codepoint.alphanumeric()).bind()
            "test ${s}@example.com"
        },
        // Strings without TLD
        arbitrary { 
            val s = Arb.string(1..10, Codepoint.alphanumeric()).bind()
            "${s}@domain"
        },
        // Just @ symbol
        Arb.constant("@"),
        // Missing local part with domain
        Arb.constant("@example.com"),
        // Missing domain
        Arb.constant("user@")
    ).bind()
}
