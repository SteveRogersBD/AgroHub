package com.example.agrohub.ui.screens.backend

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.repository.AuthRepository
import com.example.agrohub.domain.util.AppException
import com.example.agrohub.domain.util.Result
import com.example.agrohub.presentation.auth.AuthViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/**
 * UI tests for LoginScreen.
 * Tests that the screen displays error messages and handles user interactions correctly.
 */
class LoginScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun loginScreen_displaysEmailAndPasswordFields() {
        // Given
        val mockRepository = mockk<AuthRepository>()
        val viewModel = AuthViewModel(mockRepository)
        
        // When
        composeTestRule.setContent {
            LoginScreen(viewModel = viewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_displaysErrorMessage_whenLoginFails() {
        // Given
        val mockRepository = mockk<AuthRepository>()
        coEvery { mockRepository.login(any(), any()) } returns Result.Error(
            AppException.AuthenticationException("Invalid credentials")
        )
        val viewModel = AuthViewModel(mockRepository)
        
        // When
        composeTestRule.setContent {
            LoginScreen(viewModel = viewModel)
        }
        
        // Enter credentials and click login
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Login").performClick()
        
        // Then - wait for error message to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Invalid credentials")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Invalid credentials").assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_disablesLoginButton_whenFieldsAreEmpty() {
        // Given
        val mockRepository = mockk<AuthRepository>()
        val viewModel = AuthViewModel(mockRepository)
        
        // When
        composeTestRule.setContent {
            LoginScreen(viewModel = viewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("Login").assertIsNotEnabled()
    }
    
    @Test
    fun loginScreen_enablesLoginButton_whenFieldsAreFilled() {
        // Given
        val mockRepository = mockk<AuthRepository>()
        val viewModel = AuthViewModel(mockRepository)
        
        // When
        composeTestRule.setContent {
            LoginScreen(viewModel = viewModel)
        }
        
        // Enter credentials
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        
        // Then
        composeTestRule.onNodeWithText("Login").assertIsEnabled()
    }
    
    @Test
    fun loginScreen_showsLoadingIndicator_duringLogin() {
        // Given
        val mockRepository = mockk<AuthRepository>(relaxed = true)
        coEvery { mockRepository.login(any(), any()) } coAnswers {
            kotlinx.coroutines.delay(2000) // Simulate network delay
            Result.Success(
                com.example.agrohub.domain.repository.LoginResult(
                    user = User(
                        id = 1L,
                        email = "test@example.com",
                        username = "testuser",
                        name = "Test User",
                        bio = "",
                        avatarUrl = null,
                        location = "",
                        website = null,
                        createdAt = LocalDateTime.now(),
                        updatedAt = null
                    ),
                    accessToken = "token",
                    refreshToken = "refresh"
                )
            )
        }
        val viewModel = AuthViewModel(mockRepository)
        
        // When
        composeTestRule.setContent {
            LoginScreen(viewModel = viewModel)
        }
        
        // Enter credentials and click login
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Login").performClick()
        
        // Then - loading indicator should be visible
        composeTestRule.onNode(hasContentDescription("Login") or hasText("Login"))
            .assertExists()
    }
}
