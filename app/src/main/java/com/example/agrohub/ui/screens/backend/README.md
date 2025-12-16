# Backend Integration Example Screens

This directory contains example Compose screens demonstrating integration with the backend API through ViewModels.

## Screens

### 1. LoginScreen
**Location:** `LoginScreen.kt`

Demonstrates authentication with the backend:
- Email and password input fields
- Form validation (fields must not be empty)
- Loading state during login
- Error message display
- Success navigation callback
- Integration with `AuthViewModel`

**Key Features:**
- Keyboard actions for smooth UX (Next/Done)
- Disabled state during loading
- Clear error messages
- Navigation to registration

### 2. FeedScreen
**Location:** `FeedScreen.kt`

Displays a personalized feed of posts:
- Pull-to-refresh functionality using Material3's PullToRefreshBox
- Infinite scroll pagination
- Like/unlike posts with optimistic updates
- Post click navigation
- Loading states (initial, pagination)
- Empty state handling
- Error state with retry
- Integration with `FeedViewModel`

**Key Features:**
- Automatic pagination when scrolling near bottom
- Like button with filled/outlined heart icons
- Post metadata (author, timestamp, like count, comment count)
- Smooth scrolling with LazyColumn

### 3. ProfileScreen
**Location:** `ProfileScreen.kt`

Shows user profile information:
- User details (username, name, bio, location, website)
- Follow statistics (followers/following counts)
- Follow/unfollow button with toggle
- Join date display
- Loading states
- Error state with retry
- Integration with `ProfileViewModel`

**Key Features:**
- Conditional display of optional fields
- Follow stats in a card layout
- Optimistic follow/unfollow updates
- Back navigation

### 4. CreatePostScreen
**Location:** `CreatePostScreen.kt`

Allows users to create new posts:
- Multi-line content input
- Optional media URL field
- Character count display
- Form validation (content required)
- Success/error feedback via Snackbar
- Field clearing after success
- Loading state during creation
- Integration with `PostViewModel`

**Key Features:**
- Post button in both toolbar and bottom
- Disabled state during loading
- Automatic field clearing on success
- User-friendly error messages

## UI Tests

All screens have comprehensive UI tests in `app/src/androidTest/java/com/example/agrohub/ui/screens/backend/`:

### LoginScreenTest
- Displays email and password fields
- Shows error messages on login failure
- Disables/enables login button based on input
- Shows loading indicator during login

### FeedScreenTest
- Displays posts when data is loaded
- Shows empty state when no posts exist
- Displays like and comment counts
- Shows error message when load fails

### ProfileScreenTest
- Displays user information
- Shows follow statistics
- Displays follow/unfollow button
- Toggles button state when clicked

### CreatePostScreenTest
- Displays content input fields
- Validates input (disables button when empty)
- Shows character count
- Displays success/error messages
- Clears fields after successful post

## Usage Example

```kotlin
// In your navigation graph or activity
@Composable
fun BackendIntegrationDemo() {
    val authViewModel = viewModel<AuthViewModel>()
    val feedViewModel = viewModel<FeedViewModel>()
    val profileViewModel = viewModel<ProfileViewModel>()
    val postViewModel = viewModel<PostViewModel>()
    
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { navController.navigate("feed") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        
        composable("feed") {
            FeedScreen(
                viewModel = feedViewModel,
                onPostClick = { postId -> navController.navigate("post/$postId") }
            )
        }
        
        composable("profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toLong() ?: 0L
            ProfileScreen(
                viewModel = profileViewModel,
                userId = userId,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable("createPost") {
            CreatePostScreen(
                viewModel = postViewModel,
                onPostCreated = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
```

## Requirements Validated

These screens validate the following requirements from the specification:

- **20.1-20.4**: ViewModels extend AndroidX ViewModel, emit StateFlow updates, use viewModelScope, and handle errors
- **21.1-21.3**: UI states (Loading, Success, Error) are handled consistently across all screens
- All screens demonstrate proper integration with the repository layer through ViewModels
- Error messages are user-friendly and actionable
- Loading states provide visual feedback
- Success states trigger appropriate navigation or feedback

## Testing

Run the UI tests with:
```bash
./gradlew connectedAndroidTest
```

Or run specific test classes:
```bash
./gradlew connectedAndroidTest --tests "*.LoginScreenTest"
./gradlew connectedAndroidTest --tests "*.FeedScreenTest"
./gradlew connectedAndroidTest --tests "*.ProfileScreenTest"
./gradlew connectedAndroidTest --tests "*.CreatePostScreenTest"
```

## Notes

- These screens are examples demonstrating the integration pattern
- They use Material3 components for consistent design
- All screens follow the MVVM architecture pattern
- State management uses Kotlin Flow and StateFlow
- Error handling is comprehensive with user-friendly messages
- The screens are production-ready but may need styling adjustments for your app's theme
