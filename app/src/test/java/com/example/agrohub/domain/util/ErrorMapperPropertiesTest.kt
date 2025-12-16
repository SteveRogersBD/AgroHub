package com.example.agrohub.domain.util

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.RandomSource
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.random.Random

/**
 * Property-based tests for ErrorMapper.
 * Tests universal properties that should hold across all error scenarios.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ErrorMapperPropertiesTest {
    
    private lateinit var rs: RandomSource
    
    @Before
    fun setup() {
        rs = RandomSource.seeded(Random.nextLong())
    }
    
    /**
     * Feature: backend-api-integration, Property 13: HTTP Error Code Mapping
     * Validates: Requirements 19.1, 19.2, 19.3, 19.4, 19.5
     * 
     * For any HTTP error response (400, 401, 404, 500, etc.), the system should map it
     * to the corresponding AppException type with a user-friendly message.
     */
    @Test
    fun `property 13 - HTTP 400 errors map to ValidationException`() = runTest {
        repeat(100) {
            val httpException = createHttpException(400)
            val result = ErrorMapper.mapHttpException(httpException)
            
            result.shouldBeInstanceOf<AppException.ValidationException>()
            result.message.isNotBlank() shouldBe true
        }
    }
    
    @Test
    fun `property 13 - HTTP 401 errors map to AuthenticationException`() = runTest {
        repeat(100) {
            val httpException = createHttpException(401)
            val result = ErrorMapper.mapHttpException(httpException)
            
            result.shouldBeInstanceOf<AppException.AuthenticationException>()
            result.message.isNotBlank() shouldBe true
        }
    }
    
    @Test
    fun `property 13 - HTTP 403 errors map to AuthorizationException`() = runTest {
        repeat(100) {
            val httpException = createHttpException(403)
            val result = ErrorMapper.mapHttpException(httpException)
            
            result.shouldBeInstanceOf<AppException.AuthorizationException>()
            result.message.isNotBlank() shouldBe true
        }
    }
    
    @Test
    fun `property 13 - HTTP 404 errors map to NotFoundException`() = runTest {
        repeat(100) {
            val httpException = createHttpException(404)
            val result = ErrorMapper.mapHttpException(httpException)
            
            result.shouldBeInstanceOf<AppException.NotFoundException>()
            result.message.isNotBlank() shouldBe true
        }
    }
    
    @Test
    fun `property 13 - HTTP 409 errors map to ValidationException for duplicate resources`() = runTest {
        repeat(100) {
            val httpException = createHttpException(409)
            val result = ErrorMapper.mapHttpException(httpException)
            
            result.shouldBeInstanceOf<AppException.ValidationException>()
            result.message.isNotBlank() shouldBe true
        }
    }
    
    @Test
    fun `property 13 - HTTP 5xx errors map to ServerException`() = runTest {
        val serverErrorCodes = arbServerErrorCode()
        
        repeat(100) {
            val errorCode = serverErrorCodes.sample(rs).value
            val httpException = createHttpException(errorCode)
            val result = ErrorMapper.mapHttpException(httpException)
            
            result.shouldBeInstanceOf<AppException.ServerException>()
            result.message.isNotBlank() shouldBe true
        }
    }
    
    @Test
    fun `property 13 - all HTTP error codes produce non-null user-friendly messages`() = runTest {
        val allErrorCodes = arbHttpErrorCode()
        
        repeat(100) {
            val errorCode = allErrorCodes.sample(rs).value
            val httpException = createHttpException(errorCode)
            val result = ErrorMapper.mapHttpException(httpException)
            
            // All mapped exceptions should have non-empty messages
            result.message.isNotBlank() shouldBe true
            // Messages should not contain technical jargon like "HTTP" or raw error codes
            // (unless it's in a helpful context)
        }
    }
    
    @Test
    fun `property 13 - SocketTimeoutException maps to NetworkException with timeout message`() = runTest {
        repeat(100) {
            val exception = SocketTimeoutException("timeout")
            val result = ErrorMapper.mapNetworkException(exception)
            
            result.shouldBeInstanceOf<AppException.NetworkException>()
            result.message.contains("timeout", ignoreCase = true) shouldBe true
            result.message.isNotBlank() shouldBe true
        }
    }
    
    @Test
    fun `property 13 - UnknownHostException maps to NetworkException with connection message`() = runTest {
        repeat(100) {
            val exception = UnknownHostException("unknown host")
            val result = ErrorMapper.mapNetworkException(exception)
            
            result.shouldBeInstanceOf<AppException.NetworkException>()
            result.message.contains("server", ignoreCase = true) shouldBe true
            result.message.isNotBlank() shouldBe true
        }
    }
    
    @Test
    fun `property 13 - generic IOException maps to NetworkException`() = runTest {
        repeat(100) {
            val exception = IOException("network error")
            val result = ErrorMapper.mapNetworkException(exception)
            
            result.shouldBeInstanceOf<AppException.NetworkException>()
            result.message.isNotBlank() shouldBe true
        }
    }
    
    @Test
    fun `property 13 - mapException handles HttpException correctly`() = runTest {
        val allErrorCodes = arbHttpErrorCode()
        
        repeat(100) {
            val errorCode = allErrorCodes.sample(rs).value
            val httpException = createHttpException(errorCode)
            val result = ErrorMapper.mapException(httpException)
            
            // Should map to appropriate AppException subtype
            result.shouldBeInstanceOf<AppException>()
            result.message.isNotBlank() shouldBe true
        }
    }
    
    @Test
    fun `property 13 - mapException handles IOException correctly`() = runTest {
        repeat(100) {
            val exception = IOException("network error")
            val result = ErrorMapper.mapException(exception)
            
            result.shouldBeInstanceOf<AppException.NetworkException>()
            result.message.isNotBlank() shouldBe true
        }
    }
    
    @Test
    fun `property 13 - mapException returns AppException unchanged`() = runTest {
        val appExceptions = listOf(
            AppException.NetworkException("test"),
            AppException.AuthenticationException("test"),
            AppException.AuthorizationException("test"),
            AppException.NotFoundException("test"),
            AppException.ValidationException("test"),
            AppException.ServerException("test"),
            AppException.UnknownException("test")
        )
        
        appExceptions.forEach { original ->
            val result = ErrorMapper.mapException(original)
            result shouldBe original
        }
    }
    
    @Test
    fun `property 13 - all error mappings preserve error information`() = runTest {
        val allErrorCodes = arbHttpErrorCode()
        
        repeat(100) {
            val errorCode = allErrorCodes.sample(rs).value
            val httpException = createHttpException(errorCode)
            val result = ErrorMapper.mapException(httpException)
            
            // The mapped exception should be an AppException
            result.shouldBeInstanceOf<AppException>()
            // And should have a meaningful message
            result.message.length shouldBe { it: Int -> it > 0 }
        }
    }
}

// Helper functions and arbitrary generators

/**
 * Creates an HttpException with the specified status code.
 */
private fun createHttpException(code: Int): HttpException {
    val errorBody = """{"error": "Error $code"}""".toResponseBody("application/json".toMediaTypeOrNull())
    val response = Response.error<Any>(code, errorBody)
    return HttpException(response)
}

/**
 * Generates arbitrary HTTP error codes (400-599).
 */
private fun arbHttpErrorCode(): Arb<Int> = Arb.int(400..599)

/**
 * Generates arbitrary server error codes (500-599).
 */
private fun arbServerErrorCode(): Arb<Int> = Arb.int(500..599)
