package com.example.agrohub.ui.screens.backend

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.agrohub.domain.model.FeedPost
import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.model.PostAuthor
import com.example.agrohub.domain.repository.FeedRepository
import com.example.agrohub.domain.repository.LikeRepository
import com.example.agrohub.domain.util.Result
import com.example.agrohub.presentation.feed.FeedViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/**
 * UI tests for FeedScreen.
 * Tests that the screen displays posts correctly and handles user interactions.
 */
class FeedScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private fun createMockFeedPost(
        id: Long = 1L,
        username: String = "testuser",
        content: String = "Test post content",
        likeCount: Int = 5,
        commentCount: Int = 3
    ): FeedPost {
        return FeedPost(
            id = id,
            author = PostAuthor(
                id = 1L,
                username = username,
                avatarUrl = null
            ),
            content = content,
            mediaUrl = null,
            likeCount = likeCount,
            commentCount = commentCount,
            isLikedByCurrentUser = false,
            createdAt = LocalDateTime.now(),
            updatedAt = null
        )
    }
    
    @Test
    fun feedScreen_displaysPosts_whenDataIsLoaded() {
        // Given
        val mockFeedRepository = mockk<FeedRepository>()
        val mockLikeRepository = mockk<LikeRepository>()
        
        val posts = listOf(
            createMockFeedPost(id = 1L, username = "user1", content = "First post"),
            createMockFeedPost(id = 2L, username = "user2", content = "Second post")
        )
        
        coEvery { mockFeedRepository.getPersonalizedFeed(any(), any()) } returns Result.Success(
            PagedData(
                items = posts,
                currentPage = 0,
                pageSize = 10,
                totalElements = 2,
                totalPages = 1,
                isLastPage = true
            )
        )
        
        val viewModel = FeedViewModel(mockFeedRepository, mockLikeRepository)
        
        // When
        composeTestRule.setContent {
            FeedScreen(viewModel = viewModel)
        }
        
        // Then - wait for posts to load
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("First post")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("First post").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second post").assertIsDisplayed()
        composeTestRule.onNodeWithText("user1").assertIsDisplayed()
        composeTestRule.onNodeWithText("user2").assertIsDisplayed()
    }
    
    @Test
    fun feedScreen_displaysEmptyState_whenNoPostsExist() {
        // Given
        val mockFeedRepository = mockk<FeedRepository>()
        val mockLikeRepository = mockk<LikeRepository>()
        
        coEvery { mockFeedRepository.getPersonalizedFeed(any(), any()) } returns Result.Success(
            PagedData(
                items = emptyList(),
                currentPage = 0,
                pageSize = 10,
                totalElements = 0,
                totalPages = 0,
                isLastPage = true
            )
        )
        
        val viewModel = FeedViewModel(mockFeedRepository, mockLikeRepository)
        
        // When
        composeTestRule.setContent {
            FeedScreen(viewModel = viewModel)
        }
        
        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("No posts yet")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("No posts yet").assertIsDisplayed()
    }
    
    @Test
    fun feedScreen_displaysLikeAndCommentCounts() {
        // Given
        val mockFeedRepository = mockk<FeedRepository>()
        val mockLikeRepository = mockk<LikeRepository>()
        
        val post = createMockFeedPost(
            id = 1L,
            content = "Test post",
            likeCount = 42,
            commentCount = 15
        )
        
        coEvery { mockFeedRepository.getPersonalizedFeed(any(), any()) } returns Result.Success(
            PagedData(
                items = listOf(post),
                currentPage = 0,
                pageSize = 10,
                totalElements = 1,
                totalPages = 1,
                isLastPage = true
            )
        )
        
        val viewModel = FeedViewModel(mockFeedRepository, mockLikeRepository)
        
        // When
        composeTestRule.setContent {
            FeedScreen(viewModel = viewModel)
        }
        
        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Test post")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("42").assertIsDisplayed()
        composeTestRule.onNodeWithText("15 comments").assertIsDisplayed()
    }
    
    @Test
    fun feedScreen_displaysErrorMessage_whenLoadFails() {
        // Given
        val mockFeedRepository = mockk<FeedRepository>()
        val mockLikeRepository = mockk<LikeRepository>()
        
        coEvery { mockFeedRepository.getPersonalizedFeed(any(), any()) } returns Result.Error(
            com.example.agrohub.domain.util.AppException.NetworkException("Network error")
        )
        
        val viewModel = FeedViewModel(mockFeedRepository, mockLikeRepository)
        
        // When
        composeTestRule.setContent {
            FeedScreen(viewModel = viewModel)
        }
        
        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Network error")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }
}
