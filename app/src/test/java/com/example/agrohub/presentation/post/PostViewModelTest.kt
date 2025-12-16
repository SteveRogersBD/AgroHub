package com.example.agrohub.presentation.post

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.agrohub.domain.model.Comment
import com.example.agrohub.domain.model.CommentAuthor
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.model.Post
import com.example.agrohub.domain.repository.CommentRepository
import com.example.agrohub.domain.repository.PostRepository
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
 * Unit tests for PostViewModel.
 * Tests post creation, comment loading with pagination, and adding comments.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PostViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockPostRepository: PostRepository
    private lateinit var mockCommentRepository: CommentRepository
    private lateinit var viewModel: PostViewModel
    
    private val testPost = Post(
        id = 1L,
        userId = 100L,
        content = "Test post content",
        mediaUrl = null,
        createdAt = LocalDateTime.now(),
        updatedAt = null
    )
    
    private val testComment = Comment(
        id = 1L,
        postId = 1L,
        author = CommentAuthor(
            id = 100L,
            username = "testuser",
            avatarUrl = null
        ),
        content = "Test comment",
        createdAt = LocalDateTime.now(),
        updatedAt = null
    )
    
    private val testPagedComments = PagedData(
        items = listOf(testComment),
        currentPage = 0,
        pageSize = 10,
        totalElements = 1,
        totalPages = 1,
        isLastPage = true
    )
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockPostRepository = mockk()
        mockCommentRepository = mockk()
        viewModel = PostViewModel(mockPostRepository, mockCommentRepository)
    }
    
    @After
    fun teardown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `createPost success updates state to Success`() = runTest {
        // Given
        coEvery { mockPostRepository.createPost(any(), any()) } returns Result.Success(testPost)
        
        // When
        viewModel.createPost("Test post content", null)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.createPostState.value
        assertTrue(state is UiState.Success)
        assertEquals(testPost, (state as UiState.Success).data)
        coVerify { mockPostRepository.createPost("Test post content", null) }
    }
    
    @Test
    fun `createPost failure updates state to Error`() = runTest {
        // Given
        val errorMessage = "Failed to create post"
        val exception = AppException.ValidationException(errorMessage)
        coEvery { mockPostRepository.createPost(any(), any()) } returns Result.Error(exception)
        
        // When
        viewModel.createPost("Test post content", null)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.createPostState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
        coVerify { mockPostRepository.createPost("Test post content", null) }
    }
    
    @Test
    fun `createPost sets Loading state before completion`() = runTest {
        // Given
        coEvery { mockPostRepository.createPost(any(), any()) } returns Result.Success(testPost)
        
        // When
        viewModel.createPost("Test post content", null)
        
        // Then - before advanceUntilIdle, state should be Loading
        val stateBeforeCompletion = viewModel.createPostState.value
        assertTrue(stateBeforeCompletion is UiState.Loading)
        
        // Complete the coroutine
        advanceUntilIdle()
        
        // Then - after completion, state should be Success
        val stateAfterCompletion = viewModel.createPostState.value
        assertTrue(stateAfterCompletion is UiState.Success)
    }
    
    @Test
    fun `createPost with media URL succeeds`() = runTest {
        // Given
        val postWithMedia = testPost.copy(mediaUrl = "https://example.com/image.jpg")
        coEvery { mockPostRepository.createPost(any(), any()) } returns Result.Success(postWithMedia)
        
        // When
        viewModel.createPost("Test post content", "https://example.com/image.jpg")
        advanceUntilIdle()
        
        // Then
        val state = viewModel.createPostState.value
        assertTrue(state is UiState.Success)
        assertEquals(postWithMedia, (state as UiState.Success).data)
        coVerify { mockPostRepository.createPost("Test post content", "https://example.com/image.jpg") }
    }
    
    @Test
    fun `loadComments success updates state to Success`() = runTest {
        // Given
        coEvery { mockCommentRepository.getPostComments(any(), any(), any()) } returns Result.Success(testPagedComments)
        
        // When
        viewModel.loadComments(postId = 1L, page = 0)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.commentsState.value
        assertTrue(state is UiState.Success)
        assertEquals(testPagedComments, (state as UiState.Success).data)
        coVerify { mockCommentRepository.getPostComments(1L, 0, 10) }
    }
    
    @Test
    fun `loadComments failure updates state to Error`() = runTest {
        // Given
        val errorMessage = "Failed to load comments"
        val exception = AppException.NetworkException(errorMessage)
        coEvery { mockCommentRepository.getPostComments(any(), any(), any()) } returns Result.Error(exception)
        
        // When
        viewModel.loadComments(postId = 1L, page = 0)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.commentsState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
        coVerify { mockCommentRepository.getPostComments(1L, 0, 10) }
    }
    
    @Test
    fun `loadComments sets Loading state before completion`() = runTest {
        // Given
        coEvery { mockCommentRepository.getPostComments(any(), any(), any()) } returns Result.Success(testPagedComments)
        
        // When
        viewModel.loadComments(postId = 1L, page = 0)
        
        // Then - before advanceUntilIdle, state should be Loading
        val stateBeforeCompletion = viewModel.commentsState.value
        assertTrue(stateBeforeCompletion is UiState.Loading)
        
        // Complete the coroutine
        advanceUntilIdle()
        
        // Then - after completion, state should be Success
        val stateAfterCompletion = viewModel.commentsState.value
        assertTrue(stateAfterCompletion is UiState.Success)
    }
    
    @Test
    fun `loadComments with pagination loads next page`() = runTest {
        // Given
        val page1Comments = testPagedComments.copy(
            currentPage = 0,
            isLastPage = false,
            totalPages = 2
        )
        val page2Comments = testPagedComments.copy(
            currentPage = 1,
            isLastPage = true,
            totalPages = 2
        )
        
        coEvery { mockCommentRepository.getPostComments(1L, 0, 10) } returns Result.Success(page1Comments)
        coEvery { mockCommentRepository.getPostComments(1L, 1, 10) } returns Result.Success(page2Comments)
        
        // When - load first page
        viewModel.loadComments(postId = 1L, page = 0)
        advanceUntilIdle()
        
        // Then - first page loaded
        val state1 = viewModel.commentsState.value
        assertTrue(state1 is UiState.Success)
        assertEquals(0, (state1 as UiState.Success).data.currentPage)
        
        // When - load second page
        viewModel.loadComments(postId = 1L, page = 1)
        advanceUntilIdle()
        
        // Then - second page loaded
        val state2 = viewModel.commentsState.value
        assertTrue(state2 is UiState.Success)
        assertEquals(1, (state2 as UiState.Success).data.currentPage)
        
        coVerify { mockCommentRepository.getPostComments(1L, 0, 10) }
        coVerify { mockCommentRepository.getPostComments(1L, 1, 10) }
    }
    
    @Test
    fun `addComment refreshes comments from page 0`() = runTest {
        // Given
        coEvery { mockCommentRepository.createComment(any(), any()) } returns Result.Success(testComment)
        coEvery { mockCommentRepository.getPostComments(any(), any(), any()) } returns Result.Success(testPagedComments)
        
        // When
        viewModel.addComment(postId = 1L, content = "New comment")
        advanceUntilIdle()
        
        // Then - createComment was called
        coVerify { mockCommentRepository.createComment(1L, "New comment") }
        
        // Then - comments were refreshed from page 0
        coVerify { mockCommentRepository.getPostComments(1L, 0, 10) }
        
        // Then - comments state is updated
        val state = viewModel.commentsState.value
        assertTrue(state is UiState.Success)
    }
    
    @Test
    fun `addComment handles creation failure silently`() = runTest {
        // Given
        val exception = AppException.ValidationException("Comment too long")
        coEvery { mockCommentRepository.createComment(any(), any()) } returns Result.Error(exception)
        
        // When
        viewModel.addComment(postId = 1L, content = "New comment")
        advanceUntilIdle()
        
        // Then - createComment was called
        coVerify { mockCommentRepository.createComment(1L, "New comment") }
        
        // Then - comments were NOT refreshed (no call to getPostComments)
        coVerify(exactly = 0) { mockCommentRepository.getPostComments(any(), any(), any()) }
    }
    
    @Test
    fun `initial createPostState is Idle`() {
        // Then
        val state = viewModel.createPostState.value
        assertTrue(state is UiState.Idle)
    }
    
    @Test
    fun `initial commentsState is Idle`() {
        // Then
        val state = viewModel.commentsState.value
        assertTrue(state is UiState.Idle)
    }
    
    @Test
    fun `createPost with network error updates state to Error`() = runTest {
        // Given
        val errorMessage = "Network error occurred"
        val exception = AppException.NetworkException(errorMessage)
        coEvery { mockPostRepository.createPost(any(), any()) } returns Result.Error(exception)
        
        // When
        viewModel.createPost("Test post content", null)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.createPostState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }
    
    @Test
    fun `loadComments with empty result succeeds`() = runTest {
        // Given
        val emptyComments = testPagedComments.copy(
            items = emptyList(),
            totalElements = 0
        )
        coEvery { mockCommentRepository.getPostComments(any(), any(), any()) } returns Result.Success(emptyComments)
        
        // When
        viewModel.loadComments(postId = 1L, page = 0)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.commentsState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.items.isEmpty())
    }
}
