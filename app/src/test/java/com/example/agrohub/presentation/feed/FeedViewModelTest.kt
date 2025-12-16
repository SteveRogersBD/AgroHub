package com.example.agrohub.presentation.feed

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.agrohub.domain.model.FeedPost
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.model.PostAuthor
import com.example.agrohub.domain.repository.FeedRepository
import com.example.agrohub.domain.repository.LikeRepository
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
 * Unit tests for FeedViewModel.
 * Tests feed loading, pagination, refresh, and like/unlike operations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockFeedRepository: FeedRepository
    private lateinit var mockLikeRepository: LikeRepository
    private lateinit var viewModel: FeedViewModel
    
    private val testAuthor = PostAuthor(
        id = 1L,
        username = "testuser",
        avatarUrl = null
    )
    
    private val testPost1 = FeedPost(
        id = 1L,
        author = testAuthor,
        content = "Test post 1",
        mediaUrl = null,
        likeCount = 5,
        commentCount = 2,
        isLikedByCurrentUser = false,
        createdAt = LocalDateTime.now(),
        updatedAt = null
    )
    
    private val testPost2 = FeedPost(
        id = 2L,
        author = testAuthor,
        content = "Test post 2",
        mediaUrl = null,
        likeCount = 10,
        commentCount = 3,
        isLikedByCurrentUser = true,
        createdAt = LocalDateTime.now(),
        updatedAt = null
    )
    
    private val testPagedData = PagedData(
        items = listOf(testPost1, testPost2),
        currentPage = 0,
        pageSize = 10,
        totalElements = 2,
        totalPages = 1,
        isLastPage = true
    )
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockFeedRepository = mockk()
        mockLikeRepository = mockk()
        viewModel = FeedViewModel(mockFeedRepository, mockLikeRepository)
    }
    
    @After
    fun teardown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadFeed success updates state to Success`() = runTest {
        // Given
        coEvery { mockFeedRepository.getPersonalizedFeed(0, 10) } returns Result.Success(testPagedData)
        
        // When
        viewModel.loadFeed()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.feedState.value
        assertTrue(state is UiState.Success)
        assertEquals(testPagedData, (state as UiState.Success).data)
        coVerify { mockFeedRepository.getPersonalizedFeed(0, 10) }
    }
    
    @Test
    fun `loadFeed failure updates state to Error`() = runTest {
        // Given
        val errorMessage = "Network error occurred"
        val exception = AppException.NetworkException(errorMessage)
        coEvery { mockFeedRepository.getPersonalizedFeed(0, 10) } returns Result.Error(exception)
        
        // When
        viewModel.loadFeed()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.feedState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
        coVerify { mockFeedRepository.getPersonalizedFeed(0, 10) }
    }
    
    @Test
    fun `loadFeed sets Loading state before completion`() = runTest {
        // Given
        coEvery { mockFeedRepository.getPersonalizedFeed(0, 10) } returns Result.Success(testPagedData)
        
        // When
        viewModel.loadFeed()
        
        // Then - before advanceUntilIdle, state should be Loading
        val stateBeforeCompletion = viewModel.feedState.value
        assertTrue(stateBeforeCompletion is UiState.Loading)
        
        // Complete the coroutine
        advanceUntilIdle()
        
        // Then - after completion, state should be Success
        val stateAfterCompletion = viewModel.feedState.value
        assertTrue(stateAfterCompletion is UiState.Success)
    }
    
    @Test
    fun `pagination increments page number on subsequent loads`() = runTest {
        // Given
        val page0Data = testPagedData.copy(currentPage = 0, isLastPage = false)
        val page1Data = testPagedData.copy(currentPage = 1, isLastPage = true)
        
        coEvery { mockFeedRepository.getPersonalizedFeed(0, 10) } returns Result.Success(page0Data)
        coEvery { mockFeedRepository.getPersonalizedFeed(1, 10) } returns Result.Success(page1Data)
        
        // When - load first page
        viewModel.loadFeed()
        advanceUntilIdle()
        
        // Then - verify page 0 was loaded
        coVerify { mockFeedRepository.getPersonalizedFeed(0, 10) }
        
        // When - load next page
        viewModel.loadFeed()
        advanceUntilIdle()
        
        // Then - verify page 1 was loaded
        coVerify { mockFeedRepository.getPersonalizedFeed(1, 10) }
    }
    
    @Test
    fun `refresh resets page to 0`() = runTest {
        // Given
        val page0Data = testPagedData.copy(currentPage = 0, isLastPage = false)
        val page1Data = testPagedData.copy(currentPage = 1, isLastPage = false)
        
        coEvery { mockFeedRepository.getPersonalizedFeed(0, 10) } returns Result.Success(page0Data)
        coEvery { mockFeedRepository.getPersonalizedFeed(1, 10) } returns Result.Success(page1Data)
        
        // When - load first page
        viewModel.loadFeed()
        advanceUntilIdle()
        
        // When - load next page
        viewModel.loadFeed()
        advanceUntilIdle()
        
        // Verify we're on page 1
        coVerify(exactly = 1) { mockFeedRepository.getPersonalizedFeed(1, 10) }
        
        // When - refresh (should reset to page 0)
        viewModel.loadFeed(refresh = true)
        advanceUntilIdle()
        
        // Then - verify page 0 was loaded again
        coVerify(exactly = 2) { mockFeedRepository.getPersonalizedFeed(0, 10) }
    }
    
    @Test
    fun `likePost updates post like status in feed state`() = runTest {
        // Given - setup feed with unliked post
        coEvery { mockFeedRepository.getPersonalizedFeed(0, 10) } returns Result.Success(testPagedData)
        coEvery { mockLikeRepository.likePost(1L) } returns Result.Success(Unit)
        
        // Load feed first
        viewModel.loadFeed()
        advanceUntilIdle()
        
        // When - like the post
        viewModel.likePost(1L)
        advanceUntilIdle()
        
        // Then - verify like was called
        coVerify { mockLikeRepository.likePost(1L) }
        
        // Then - verify state was updated
        val state = viewModel.feedState.value
        assertTrue(state is UiState.Success)
        val updatedPost = (state as UiState.Success).data.items.find { it.id == 1L }
        assertTrue(updatedPost?.isLikedByCurrentUser == true)
        assertEquals(6, updatedPost?.likeCount) // Should increment from 5 to 6
    }
    
    @Test
    fun `unlikePost updates post like status in feed state`() = runTest {
        // Given - setup feed with liked post
        coEvery { mockFeedRepository.getPersonalizedFeed(0, 10) } returns Result.Success(testPagedData)
        coEvery { mockLikeRepository.unlikePost(2L) } returns Result.Success(Unit)
        
        // Load feed first
        viewModel.loadFeed()
        advanceUntilIdle()
        
        // When - unlike the post
        viewModel.unlikePost(2L)
        advanceUntilIdle()
        
        // Then - verify unlike was called
        coVerify { mockLikeRepository.unlikePost(2L) }
        
        // Then - verify state was updated
        val state = viewModel.feedState.value
        assertTrue(state is UiState.Success)
        val updatedPost = (state as UiState.Success).data.items.find { it.id == 2L }
        assertTrue(updatedPost?.isLikedByCurrentUser == false)
        assertEquals(9, updatedPost?.likeCount) // Should decrement from 10 to 9
    }
    
    @Test
    fun `likePost with error does not update state`() = runTest {
        // Given - setup feed
        coEvery { mockFeedRepository.getPersonalizedFeed(0, 10) } returns Result.Success(testPagedData)
        coEvery { mockLikeRepository.likePost(1L) } returns Result.Error(AppException.NetworkException())
        
        // Load feed first
        viewModel.loadFeed()
        advanceUntilIdle()
        
        val stateBeforeLike = viewModel.feedState.value
        
        // When - like the post (which will fail)
        viewModel.likePost(1L)
        advanceUntilIdle()
        
        // Then - verify like was called
        coVerify { mockLikeRepository.likePost(1L) }
        
        // Then - verify state was NOT updated (remains the same)
        val stateAfterLike = viewModel.feedState.value
        assertEquals(stateBeforeLike, stateAfterLike)
    }
    
    @Test
    fun `unlikePost with error does not update state`() = runTest {
        // Given - setup feed
        coEvery { mockFeedRepository.getPersonalizedFeed(0, 10) } returns Result.Success(testPagedData)
        coEvery { mockLikeRepository.unlikePost(2L) } returns Result.Error(AppException.NetworkException())
        
        // Load feed first
        viewModel.loadFeed()
        advanceUntilIdle()
        
        val stateBeforeUnlike = viewModel.feedState.value
        
        // When - unlike the post (which will fail)
        viewModel.unlikePost(2L)
        advanceUntilIdle()
        
        // Then - verify unlike was called
        coVerify { mockLikeRepository.unlikePost(2L) }
        
        // Then - verify state was NOT updated (remains the same)
        val stateAfterUnlike = viewModel.feedState.value
        assertEquals(stateBeforeUnlike, stateAfterUnlike)
    }
    
    @Test
    fun `initial feedState is Idle`() {
        // Then
        val state = viewModel.feedState.value
        assertTrue(state is UiState.Idle)
    }
    
    @Test
    fun `pagination does not increment on last page`() = runTest {
        // Given - last page of data
        val lastPageData = testPagedData.copy(isLastPage = true)
        coEvery { mockFeedRepository.getPersonalizedFeed(0, 10) } returns Result.Success(lastPageData)
        
        // When - load feed
        viewModel.loadFeed()
        advanceUntilIdle()
        
        // When - try to load next page
        viewModel.loadFeed()
        advanceUntilIdle()
        
        // Then - page 0 should still be requested (page didn't increment)
        coVerify(exactly = 2) { mockFeedRepository.getPersonalizedFeed(0, 10) }
    }
    
    @Test
    fun `loadFeed with authentication error updates state to Error`() = runTest {
        // Given
        val errorMessage = "Authentication failed"
        val exception = AppException.AuthenticationException(errorMessage)
        coEvery { mockFeedRepository.getPersonalizedFeed(0, 10) } returns Result.Error(exception)
        
        // When
        viewModel.loadFeed()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.feedState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }
}
