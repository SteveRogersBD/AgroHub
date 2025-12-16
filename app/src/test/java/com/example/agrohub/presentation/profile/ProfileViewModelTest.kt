package com.example.agrohub.presentation.profile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.agrohub.domain.model.FollowStats
import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.repository.FollowRepository
import com.example.agrohub.domain.repository.UserRepository
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
 * Unit tests for ProfileViewModel.
 * Tests profile loading, follow stats loading, and follow/unfollow operations with various success and error scenarios.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockFollowRepository: FollowRepository
    private lateinit var viewModel: ProfileViewModel
    
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
    
    private val testFollowStats = FollowStats(
        followersCount = 100,
        followingCount = 50
    )
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockUserRepository = mockk()
        mockFollowRepository = mockk()
        viewModel = ProfileViewModel(mockUserRepository, mockFollowRepository)
    }
    
    @After
    fun teardown() {
        Dispatchers.resetMain()
    }
    
    // Profile Loading Tests
    
    @Test
    fun `loadProfile success updates state to Success`() = runTest {
        // Given
        coEvery { mockUserRepository.getUserById(any()) } returns Result.Success(testUser)
        
        // When
        viewModel.loadProfile(1L)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.profileState.value
        assertTrue(state is UiState.Success)
        assertEquals(testUser, (state as UiState.Success).data)
        coVerify { mockUserRepository.getUserById(1L) }
    }
    
    @Test
    fun `loadProfile failure updates state to Error`() = runTest {
        // Given
        val errorMessage = "User not found"
        val exception = AppException.NotFoundException(errorMessage)
        coEvery { mockUserRepository.getUserById(any()) } returns Result.Error(exception)
        
        // When
        viewModel.loadProfile(1L)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.profileState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
        coVerify { mockUserRepository.getUserById(1L) }
    }
    
    @Test
    fun `loadProfile sets Loading state before completion`() = runTest {
        // Given
        coEvery { mockUserRepository.getUserById(any()) } returns Result.Success(testUser)
        
        // When
        viewModel.loadProfile(1L)
        
        // Then - before advanceUntilIdle, state should be Loading
        val stateBeforeCompletion = viewModel.profileState.value
        assertTrue(stateBeforeCompletion is UiState.Loading)
        
        // Complete the coroutine
        advanceUntilIdle()
        
        // Then - after completion, state should be Success
        val stateAfterCompletion = viewModel.profileState.value
        assertTrue(stateAfterCompletion is UiState.Success)
    }
    
    @Test
    fun `initial profileState is Idle`() {
        // Then
        val state = viewModel.profileState.value
        assertTrue(state is UiState.Idle)
    }
    
    // Follow Stats Loading Tests
    
    @Test
    fun `loadFollowStats success updates state to Success`() = runTest {
        // Given
        coEvery { mockFollowRepository.getFollowStats(any()) } returns Result.Success(testFollowStats)
        
        // When
        viewModel.loadFollowStats(1L)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.followStatsState.value
        assertTrue(state is UiState.Success)
        assertEquals(testFollowStats, (state as UiState.Success).data)
        coVerify { mockFollowRepository.getFollowStats(1L) }
    }
    
    @Test
    fun `loadFollowStats failure updates state to Error`() = runTest {
        // Given
        val errorMessage = "Failed to load stats"
        val exception = AppException.ServerException(errorMessage)
        coEvery { mockFollowRepository.getFollowStats(any()) } returns Result.Error(exception)
        
        // When
        viewModel.loadFollowStats(1L)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.followStatsState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
        coVerify { mockFollowRepository.getFollowStats(1L) }
    }
    
    @Test
    fun `loadFollowStats sets Loading state before completion`() = runTest {
        // Given
        coEvery { mockFollowRepository.getFollowStats(any()) } returns Result.Success(testFollowStats)
        
        // When
        viewModel.loadFollowStats(1L)
        
        // Then - before advanceUntilIdle, state should be Loading
        val stateBeforeCompletion = viewModel.followStatsState.value
        assertTrue(stateBeforeCompletion is UiState.Loading)
        
        // Complete the coroutine
        advanceUntilIdle()
        
        // Then - after completion, state should be Success
        val stateAfterCompletion = viewModel.followStatsState.value
        assertTrue(stateAfterCompletion is UiState.Success)
    }
    
    @Test
    fun `initial followStatsState is Idle`() {
        // Then
        val state = viewModel.followStatsState.value
        assertTrue(state is UiState.Idle)
    }
    
    // Follow/Unfollow Tests
    
    @Test
    fun `followUser updates stats by incrementing followers count`() = runTest {
        // Given - set initial stats state
        coEvery { mockFollowRepository.getFollowStats(any()) } returns Result.Success(testFollowStats)
        viewModel.loadFollowStats(1L)
        advanceUntilIdle()
        
        // Verify initial state
        val initialState = viewModel.followStatsState.value
        assertTrue(initialState is UiState.Success)
        assertEquals(100, (initialState as UiState.Success).data.followersCount)
        
        // Given - follow operation succeeds
        coEvery { mockFollowRepository.followUser(any()) } returns Result.Success(Unit)
        
        // When
        viewModel.followUser(1L)
        advanceUntilIdle()
        
        // Then - followers count should be incremented
        val updatedState = viewModel.followStatsState.value
        assertTrue(updatedState is UiState.Success)
        assertEquals(101, (updatedState as UiState.Success).data.followersCount)
        assertEquals(50, updatedState.data.followingCount) // followingCount unchanged
        coVerify { mockFollowRepository.followUser(1L) }
    }
    
    @Test
    fun `unfollowUser updates stats by decrementing followers count`() = runTest {
        // Given - set initial stats state
        coEvery { mockFollowRepository.getFollowStats(any()) } returns Result.Success(testFollowStats)
        viewModel.loadFollowStats(1L)
        advanceUntilIdle()
        
        // Verify initial state
        val initialState = viewModel.followStatsState.value
        assertTrue(initialState is UiState.Success)
        assertEquals(100, (initialState as UiState.Success).data.followersCount)
        
        // Given - unfollow operation succeeds
        coEvery { mockFollowRepository.unfollowUser(any()) } returns Result.Success(Unit)
        
        // When
        viewModel.unfollowUser(1L)
        advanceUntilIdle()
        
        // Then - followers count should be decremented
        val updatedState = viewModel.followStatsState.value
        assertTrue(updatedState is UiState.Success)
        assertEquals(99, (updatedState as UiState.Success).data.followersCount)
        assertEquals(50, updatedState.data.followingCount) // followingCount unchanged
        coVerify { mockFollowRepository.unfollowUser(1L) }
    }
    
    @Test
    fun `followUser does not update stats when follow operation fails`() = runTest {
        // Given - set initial stats state
        coEvery { mockFollowRepository.getFollowStats(any()) } returns Result.Success(testFollowStats)
        viewModel.loadFollowStats(1L)
        advanceUntilIdle()
        
        // Verify initial state
        val initialState = viewModel.followStatsState.value
        assertTrue(initialState is UiState.Success)
        assertEquals(100, (initialState as UiState.Success).data.followersCount)
        
        // Given - follow operation fails
        val exception = AppException.NetworkException("Network error")
        coEvery { mockFollowRepository.followUser(any()) } returns Result.Error(exception)
        
        // When
        viewModel.followUser(1L)
        advanceUntilIdle()
        
        // Then - stats should remain unchanged
        val updatedState = viewModel.followStatsState.value
        assertTrue(updatedState is UiState.Success)
        assertEquals(100, (updatedState as UiState.Success).data.followersCount)
        coVerify { mockFollowRepository.followUser(1L) }
    }
    
    @Test
    fun `unfollowUser does not update stats when unfollow operation fails`() = runTest {
        // Given - set initial stats state
        coEvery { mockFollowRepository.getFollowStats(any()) } returns Result.Success(testFollowStats)
        viewModel.loadFollowStats(1L)
        advanceUntilIdle()
        
        // Verify initial state
        val initialState = viewModel.followStatsState.value
        assertTrue(initialState is UiState.Success)
        assertEquals(100, (initialState as UiState.Success).data.followersCount)
        
        // Given - unfollow operation fails
        val exception = AppException.NetworkException("Network error")
        coEvery { mockFollowRepository.unfollowUser(any()) } returns Result.Error(exception)
        
        // When
        viewModel.unfollowUser(1L)
        advanceUntilIdle()
        
        // Then - stats should remain unchanged
        val updatedState = viewModel.followStatsState.value
        assertTrue(updatedState is UiState.Success)
        assertEquals(100, (updatedState as UiState.Success).data.followersCount)
        coVerify { mockFollowRepository.unfollowUser(1L) }
    }
    
    @Test
    fun `followUser does not update stats when stats state is not Success`() = runTest {
        // Given - stats state is Idle (not loaded yet)
        val initialState = viewModel.followStatsState.value
        assertTrue(initialState is UiState.Idle)
        
        // Given - follow operation succeeds
        coEvery { mockFollowRepository.followUser(any()) } returns Result.Success(Unit)
        
        // When
        viewModel.followUser(1L)
        advanceUntilIdle()
        
        // Then - stats state should remain Idle (no update)
        val updatedState = viewModel.followStatsState.value
        assertTrue(updatedState is UiState.Idle)
        coVerify { mockFollowRepository.followUser(1L) }
    }
    
    @Test
    fun `initial userPostsState is Idle`() {
        // Then
        val state = viewModel.userPostsState.value
        assertTrue(state is UiState.Idle)
    }
    
    @Test
    fun `loadProfile with network error updates state to Error`() = runTest {
        // Given
        val errorMessage = "Network error occurred"
        val exception = AppException.NetworkException(errorMessage)
        coEvery { mockUserRepository.getUserById(any()) } returns Result.Error(exception)
        
        // When
        viewModel.loadProfile(1L)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.profileState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }
    
    @Test
    fun `multiple followUser calls increment stats correctly`() = runTest {
        // Given - set initial stats state
        coEvery { mockFollowRepository.getFollowStats(any()) } returns Result.Success(testFollowStats)
        viewModel.loadFollowStats(1L)
        advanceUntilIdle()
        
        // Given - follow operation succeeds
        coEvery { mockFollowRepository.followUser(any()) } returns Result.Success(Unit)
        
        // When - follow twice
        viewModel.followUser(1L)
        advanceUntilIdle()
        viewModel.followUser(1L)
        advanceUntilIdle()
        
        // Then - followers count should be incremented twice
        val updatedState = viewModel.followStatsState.value
        assertTrue(updatedState is UiState.Success)
        assertEquals(102, (updatedState as UiState.Success).data.followersCount)
    }
}
