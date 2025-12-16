package com.example.agrohub.ui.screens.backend

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.agrohub.domain.model.Post
import com.example.agrohub.domain.repository.CommentRepository
import com.example.agrohub.domain.repository.PostRepository
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import com.example.agrohub.presentation.post.PostViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/**
 * UI tests for CreatePostScreen.
 * Tests that the screen validates input and displays feedback correctly.
 */
class CreatePostScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun createPostScreen_displaysContentInput() {
        // Given
        val mockPostRepository = mockk<PostRepository>()
        val mockCommentRepository = mockk<CommentRepository>()
        val viewModel = PostViewModel(mockPostRepository, mockCommentRepository)
        
        // When
        composeTestRule.setContent {
            CreatePostScreen(viewModel = viewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("What's on your mind?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Media URL (optional)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Post").assertIsDisplayed()
    }
    
    @Test
    fun createPostScreen_disablesPostButton_whenContentIsEmpty() {
        // Given
        val mockPostRepository = mockk<PostRepository>()
        val mockCommentRepository = mockk<CommentRepository>()
        val viewModel = PostViewModel(mockPostRepository, mockCommentRepository)
        
        // When
        composeTestRule.setContent {
            CreatePostScreen(viewModel = viewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("Create Post").assertIsNotEnabled()
    }
    
    @Test
    fun createPostScreen_enablesPostButton_whenContentIsProvided() {
        // Given
        val mockPostRepository = mockk<PostRepository>()
        val mockCommentRepository = mockk<CommentRepository>()
        val viewModel = PostViewModel(mockPostRepository, mockCommentRepository)
        
        // When
        composeTestRule.setContent {
            CreatePostScreen(viewModel = viewModel)
        }
        
        // Enter content
        composeTestRule.onNodeWithText("What's on your mind?").performTextInput("Test post content")
        
        // Then
        composeTestRule.onNodeWithText("Create Post").assertIsEnabled()
    }
    
    @Test
    fun createPostScreen_displaysCharacterCount() {
        // Given
        val mockPostRepository = mockk<PostRepository>()
        val mockCommentRepository = mockk<CommentRepository>()
        val viewModel = PostViewModel(mockPostRepository, mockCommentRepository)
        
        // When
        composeTestRule.setContent {
            CreatePostScreen(viewModel = viewModel)
        }
        
        // Initially shows 0 characters
        composeTestRule.onNodeWithText("0 characters").assertIsDisplayed()
        
        // Enter content
        composeTestRule.onNodeWithText("What's on your mind?").performTextInput("Hello")
        
        // Then - should show 5 characters
        composeTestRule.onNodeWithText("5 characters").assertIsDisplayed()
    }
    
    @Test
    fun createPostScreen_showsSuccessMessage_whenPostIsCreated() {
        // Given
        val mockPostRepository = mockk<PostRepository>()
        val mockCommentRepository = mockk<CommentRepository>()
        
        coEvery { mockPostRepository.createPost(any(), any()) } returns Result.Success(
            Post(
                id = 1L,
                userId = 1L,
                content = "Test post",
                mediaUrl = null,
                createdAt = LocalDateTime.now(),
                updatedAt = null
            )
        )
        
        val viewModel = PostViewModel(mockPostRepository, mockCommentRepository)
        
        // When
        composeTestRule.setContent {
            CreatePostScreen(viewModel = viewModel)
        }
        
        // Enter content and create post
        composeTestRule.onNodeWithText("What's on your mind?").performTextInput("Test post")
        composeTestRule.onNodeWithText("Create Post").performClick()
        
        // Then - success message should appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Post created successfully!")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Post created successfully!").assertIsDisplayed()
    }
    
    @Test
    fun createPostScreen_showsErrorMessage_whenPostCreationFails() {
        // Given
        val mockPostRepository = mockk<PostRepository>()
        val mockCommentRepository = mockk<CommentRepository>()
        
        coEvery { mockPostRepository.createPost(any(), any()) } returns Result.Error(
            AppException.ValidationException("Content is too long")
        )
        
        val viewModel = PostViewModel(mockPostRepository, mockCommentRepository)
        
        // When
        composeTestRule.setContent {
            CreatePostScreen(viewModel = viewModel)
        }
        
        // Enter content and create post
        composeTestRule.onNodeWithText("What's on your mind?").performTextInput("Test post")
        composeTestRule.onNodeWithText("Create Post").performClick()
        
        // Then - error message should appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Content is too long")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Content is too long").assertIsDisplayed()
    }
    
    @Test
    fun createPostScreen_clearsFields_afterSuccessfulPost() {
        // Given
        val mockPostRepository = mockk<PostRepository>()
        val mockCommentRepository = mockk<CommentRepository>()
        
        coEvery { mockPostRepository.createPost(any(), any()) } returns Result.Success(
            Post(
                id = 1L,
                userId = 1L,
                content = "Test post",
                mediaUrl = null,
                createdAt = LocalDateTime.now(),
                updatedAt = null
            )
        )
        
        val viewModel = PostViewModel(mockPostRepository, mockCommentRepository)
        
        // When
        composeTestRule.setContent {
            CreatePostScreen(viewModel = viewModel)
        }
        
        // Enter content and create post
        composeTestRule.onNodeWithText("What's on your mind?").performTextInput("Test post")
        composeTestRule.onNodeWithText("Media URL (optional)").performTextInput("https://example.com/image.jpg")
        composeTestRule.onNodeWithText("Create Post").performClick()
        
        // Wait for success
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Post created successfully!")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // Then - fields should be cleared
        composeTestRule.onNodeWithText("0 characters").assertIsDisplayed()
    }
}
