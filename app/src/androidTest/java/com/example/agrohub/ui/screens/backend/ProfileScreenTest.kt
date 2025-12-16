package com.example.agrohub.ui.screens.backend

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.agrohub.domain.model.FollowStats
import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.repository.FollowRepository
import com.example.agrohub.domain.repository.UserRepository
import com.example.agrohub.domain.util.Result
import com.example.agrohub.presentation.profile.ProfileViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/**
 * UI tests for ProfileScreen.
 * Tests that the screen displays user information correctly.
 */
class ProfileScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private fun createMockUser(
        id: Long = 1L,
        username: String = "testuser",
        name: String = "Test User",
        bio: String = "Test bio",
        location: String = "Test Location"
    ): User {
        return User(
            id = id,
            email = "test@example.com",
            username = username,
            name = name,
            bio = bio,
            avatarUrl = null,
            location = location,
            website = "https://example.com",
            createdAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            updatedAt = null
        )
    }
    
    @Test
    fun profileScreen_displaysUserInfo_whenDataIsLoaded() {
        // Given
        val mockUserRepository = mockk<UserRepository>()
        val mockFollowRepository = mockk<FollowRepository>()
        
        val user = createMockUser(
            username = "johndoe",
            name = "John Doe",
            bio = "Software developer",
            location = "San Francisco"
        )
        
        coEvery { mockUserRepository.getUserById(any()) } returns Result.Success(user)
        coEvery { mockFollowRepository.getFollowStats(any()) } returns Result.Success(
            FollowStats(followersCount = 100, followingCount = 50)
        )
        
        val viewModel = ProfileViewModel(mockUserRepository, mockFollowRepository)
        
        // When
        composeTestRule.setContent {
            ProfileScreen(viewModel = viewModel, userId = 1L)
        }
        
        // Then - wait for profile to load
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("johndoe")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("johndoe").assertIsDisplayed()
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Software developer").assertIsDisplayed()
        composeTestRule.onNodeWithText("📍 San Francisco").assertIsDisplayed()
        composeTestRule.onNodeWithText("🔗 https://example.com").assertIsDisplayed()
    }
    
    @Test
    fun profileScreen_displaysFollowStats_whenDataIsLoaded() {
        // Given
        val mockUserRepository = mockk<UserRepository>()
        val mockFollowRepository = mockk<FollowRepository>()
        
        val user = createMockUser()
        
        coEvery { mockUserRepository.getUserById(any()) } returns Result.Success(user)
        coEvery { mockFollowRepository.getFollowStats(any()) } returns Result.Success(
            FollowStats(followersCount = 250, followingCount = 180)
        )
        
        val viewModel = ProfileViewModel(mockUserRepository, mockFollowRepository)
        
        // When
        composeTestRule.setContent {
            ProfileScreen(viewModel = viewModel, userId = 1L)
        }
        
        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("250")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("250").assertIsDisplayed()
        composeTestRule.onNodeWithText("Followers").assertIsDisplayed()
        composeTestRule.onNodeWithText("180").assertIsDisplayed()
        composeTestRule.onNodeWithText("Following").assertIsDisplayed()
    }
    
    @Test
    fun profileScreen_displaysFollowButton() {
        // Given
        val mockUserRepository = mockk<UserRepository>()
        val mockFollowRepository = mockk<FollowRepository>()
        
        val user = createMockUser()
        
        coEvery { mockUserRepository.getUserById(any()) } returns Result.Success(user)
        coEvery { mockFollowRepository.getFollowStats(any()) } returns Result.Success(
            FollowStats(followersCount = 100, followingCount = 50)
        )
        
        val viewModel = ProfileViewModel(mockUserRepository, mockFollowRepository)
        
        // When
        composeTestRule.setContent {
            ProfileScreen(viewModel = viewModel, userId = 1L)
        }
        
        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Follow")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("Follow").assertIsDisplayed()
    }
    
    @Test
    fun profileScreen_togglesFollowButton_whenClicked() {
        // Given
        val mockUserRepository = mockk<UserRepository>()
        val mockFollowRepository = mockk<FollowRepository>(relaxed = true)
        
        val user = createMockUser()
        
        coEvery { mockUserRepository.getUserById(any()) } returns Result.Success(user)
        coEvery { mockFollowRepository.getFollowStats(any()) } returns Result.Success(
            FollowStats(followersCount = 100, followingCount = 50)
        )
        coEvery { mockFollowRepository.followUser(any()) } returns Result.Success(Unit)
        
        val viewModel = ProfileViewModel(mockUserRepository, mockFollowRepository)
        
        // When
        composeTestRule.setContent {
            ProfileScreen(viewModel = viewModel, userId = 1L)
        }
        
        // Wait for Follow button to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Follow")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // Click follow button
        composeTestRule.onNodeWithText("Follow").performClick()
        
        // Then - button should change to Unfollow
        composeTestRule.onNodeWithText("Unfollow").assertIsDisplayed()
    }
}
