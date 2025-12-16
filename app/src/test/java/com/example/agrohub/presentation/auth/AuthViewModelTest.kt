package com.example.agrohub.presentation.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.repository.AuthRepository
import com.example.agrohub.domain.repository.LoginResult
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import com.example.agrohub.domain.util.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for AuthViewModel.
 * Tests login, register, and logout operations with various success and error scenarios.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel
    
    private val testUser = User(
        id = 1L,
        email = "test@example.com",
        username = "testuser",
        name = "Test User",
        bio = "Test bio",
        avatarUrl = null,
        location = "Test Location",
        website = null,
        createdAt = LocalDateTime.now(),
        updatedAt = null
    )
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk()
        viewModel = AuthViewModel(mockRepository)
    }
    
    @After
    fun teardown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `login success updates state to Success`() = runTest {
        // Given
        val loginResult = LoginResult(
            user = testUser,
            accessToken = "access_token",
            refreshToken = "refresh_token"
        )
        coEvery { mockRepository.login(any(), any()) } returns Result.Success(loginResult)
        
        // When
        viewModel.login("test@example.com", "password123")
        advanceUntilIdle()
        
        // Then
        val state = viewModel.loginState.value
        assertTrue(state is UiState.Success)
        assertEquals(testUser, (state as UiState.Success).data)
        coVerify { mockRepository.login("test@example.com", "password123") }
    }
    
    @Test
    fun `login failure updates state to Error`() = runTest {
        // Given
        val errorMessage = "Invalid credentials"
        val exception = AppException.AuthenticationException(errorMessage)
        coEvery { mockRepository.login(any(), any()) } returns Result.Error(exception)
        
        // When
        viewModel.login("test@example.com", "wrongpassword")
        advanceUntilIdle()
        
        // Then
        val state = viewModel.loginState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
        coVerify { mockRepository.login("test@example.com", "wrongpassword") }
    }
    
    @Test
    fun `login sets Loading state before completion`() = runTest {
        // Given
        val loginResult = LoginResult(
            user = testUser,
            accessToken = "access_token",
            refreshToken = "refresh_token"
        )
        coEvery { mockRepository.login(any(), any()) } returns Result.Success(loginResult)
        
        // When
        viewModel.login("test@example.com", "password123")
        
        // Then - before advanceUntilIdle, state should be Loading
        val stateBeforeCompletion = viewModel.loginState.value
        assertTrue(stateBeforeCompletion is UiState.Loading)
        
        // Complete the coroutine
        advanceUntilIdle()
        
        // Then - after completion, state should be Success
        val stateAfterCompletion = viewModel.loginState.value
        assertTrue(stateAfterCompletion is UiState.Success)
    }
    
    @Test
    fun `register success updates state to Success`() = runTest {
        // Given
        coEvery { mockRepository.register(any(), any(), any()) } returns Result.Success(testUser)
        
        // When
        viewModel.register("test@example.com", "testuser", "password123")
        advanceUntilIdle()
        
        // Then
        val state = viewModel.registerState.value
        assertTrue(state is UiState.Success)
        assertEquals(testUser, (state as UiState.Success).data)
        coVerify { mockRepository.register("test@example.com", "testuser", "password123") }
    }
    
    @Test
    fun `register failure updates state to Error`() = runTest {
        // Given
        val errorMessage = "Email already exists"
        val exception = AppException.ValidationException(errorMessage)
        coEvery { mockRepository.register(any(), any(), any()) } returns Result.Error(exception)
        
        // When
        viewModel.register("existing@example.com", "testuser", "password123")
        advanceUntilIdle()
        
        // Then
        val state = viewModel.registerState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
        coVerify { mockRepository.register("existing@example.com", "testuser", "password123") }
    }
    
    @Test
    fun `register sets Loading state before completion`() = runTest {
        // Given
        coEvery { mockRepository.register(any(), any(), any()) } returns Result.Success(testUser)
        
        // When
        viewModel.register("test@example.com", "testuser", "password123")
        
        // Then - before advanceUntilIdle, state should be Loading
        val stateBeforeCompletion = viewModel.registerState.value
        assertTrue(stateBeforeCompletion is UiState.Loading)
        
        // Complete the coroutine
        advanceUntilIdle()
        
        // Then - after completion, state should be Success
        val stateAfterCompletion = viewModel.registerState.value
        assertTrue(stateAfterCompletion is UiState.Success)
    }
    
    @Test
    fun `logout clears authentication`() = runTest {
        // Given
        coEvery { mockRepository.logout() } returns Result.Success(Unit)
        
        // When
        viewModel.logout()
        advanceUntilIdle()
        
        // Then
        coVerify { mockRepository.logout() }
    }
    
    @Test
    fun `initial loginState is Idle`() {
        // Then
        val state = viewModel.loginState.value
        assertTrue(state is UiState.Idle)
    }
    
    @Test
    fun `initial registerState is Idle`() {
        // Then
        val state = viewModel.registerState.value
        assertTrue(state is UiState.Idle)
    }
    
    @Test
    fun `login with network error updates state to Error`() = runTest {
        // Given
        val errorMessage = "Network error occurred"
        val exception = AppException.NetworkException(errorMessage)
        coEvery { mockRepository.login(any(), any()) } returns Result.Error(exception)
        
        // When
        viewModel.login("test@example.com", "password123")
        advanceUntilIdle()
        
        // Then
        val state = viewModel.loginState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }
    
    @Test
    fun `register with validation error updates state to Error`() = runTest {
        // Given
        val errorMessage = "Password must be at least 8 characters"
        val exception = AppException.ValidationException(errorMessage)
        coEvery { mockRepository.register(any(), any(), any()) } returns Result.Error(exception)
        
        // When
        viewModel.register("test@example.com", "testuser", "short")
        advanceUntilIdle()
        
        // Then
        val state = viewModel.registerState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }
}
