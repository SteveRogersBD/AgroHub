package com.example.agrohub.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.example.agrohub.data.remote.NetworkModule
import com.example.agrohub.data.repository.CommentRepositoryImpl
import com.example.agrohub.data.repository.FeedRepositoryImpl
import com.example.agrohub.data.repository.LikeRepositoryImpl
import com.example.agrohub.data.repository.PostRepositoryImpl
import com.example.agrohub.presentation.feed.FeedViewModel
import com.example.agrohub.presentation.post.PostViewModel
import com.example.agrohub.ui.screens.addfarm.AddFarmScreen
import com.example.agrohub.ui.screens.auth.WorkingSignInScreen
import com.example.agrohub.ui.screens.auth.WorkingSignUpScreen
import com.example.agrohub.ui.screens.chat.ChatScreen
import com.example.agrohub.ui.screens.community.CommunityScreen
import com.example.agrohub.ui.screens.community.CreatePostScreen
import com.example.agrohub.ui.screens.disease.DiseaseDetectionScreen
import com.example.agrohub.ui.screens.disease.DiseaseResultScreen
import com.example.agrohub.ui.screens.farm.FieldMapScreen
import com.example.agrohub.ui.screens.home.HomeScreen
import com.example.agrohub.ui.screens.marketplace.MarketplaceScreen
import com.example.agrohub.ui.screens.marketplace.ProductDetailScreen
import com.example.agrohub.ui.screens.profile.ProfileScreen
import com.example.agrohub.ui.screens.weather.WeatherScreen

/**
 * Main navigation component for the AgroHub application.
 * Configures NavHost with all screen routes and handles navigation state.
 * 
 * Requirements: 10.1, 10.3, 10.5
 * 
 * @param modifier Optional modifier for the navigation container
 * @param navController Optional NavController for testing purposes
 */
@Composable
fun AgroHubNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.SIGN_IN
    
    // Check if user is authenticated and navigate accordingly
    val tokenManager = NetworkModule.provideTokenManager(context)
    
    // Check authentication status on first launch
    LaunchedEffect(Unit) {
        try {
            val token = tokenManager.getAccessToken()
            val isAuthenticated = token?.isNotBlank() == true
            
            // If authenticated and currently on sign in screen, navigate to home
            if (isAuthenticated && currentRoute == Routes.SIGN_IN) {
                // Sync username to shared prefs for FieldRepository
                var usernameSynced = false
                if (tokenManager is com.example.agrohub.security.TokenManagerImpl) {
                    val username = tokenManager.getUsername()
                    if (!username.isNullOrBlank()) {
                        val prefs = context.getSharedPreferences("agrohub_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putString("username", username).apply()
                        println("AgroHubNavigation: Synced username $username to legacy prefs")
                        usernameSynced = true
                    }
                }

                if (usernameSynced) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SIGN_IN) { inclusive = true }
                    }
                } else {
                    // Critical error: Authenticated but no username found. Force re-login.
                    println("AgroHubNavigation: Authenticated but no username found! Clearing tokens.")
                    tokenManager.clearTokens()
                    // Stay on SignIn screen (isAuthenticated will be false on next check or UI update)
                }
            }
        } catch (e: Exception) {
            // Stay on sign in screen if error
        }
    }
    
    // Determine if bottom navigation should be visible
    val showBottomNav = currentRoute in Routes.bottomNavItems
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(Routes.HOME) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.SIGN_IN,
            modifier = Modifier.padding(paddingValues),
            // Screen transition animations
            enterTransition = {
                // Fade in animation
                fadeIn(animationSpec = tween(300)) +
                        // Slide in from right
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
            },
            exitTransition = {
                // Fade out animation
                fadeOut(animationSpec = tween(300)) +
                        // Slide out to left
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
            },
            popEnterTransition = {
                // Fade in animation when popping back
                fadeIn(animationSpec = tween(300)) +
                        // Slide in from left
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
            },
            popExitTransition = {
                // Fade out animation when popping back
                fadeOut(animationSpec = tween(300)) +
                        // Slide out to right
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
            }
        ) {
            // Sign In Screen
            composable(Routes.SIGN_IN) {
                WorkingSignInScreen(navController = navController)
            }
            
            // Sign Up Screen
            composable(Routes.SIGN_UP) {
                WorkingSignUpScreen(navController = navController)
            }
            
            // Home Screen
            composable(Routes.HOME) {
                HomeScreen(navController = navController)
            }
            
            // Disease Detection/Scan Screen
            composable(Routes.SCAN) {
                DiseaseDetectionScreen(navController = navController)
            }
            
            // Disease Result Screen
            composable("disease_result_screen") {
                val context = androidx.compose.ui.platform.LocalContext.current
                val viewModel = remember { 
                    com.example.agrohub.presentation.disease.DiseaseDetectionViewModelFactory.getInstance(context)
                }
                
                DiseaseResultScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
            
            // Weather Screen
            composable(Routes.WEATHER) {
                WeatherScreen(navController = navController)
            }
            
            // Weather Detail Screen
            composable("weather_detail/{dayIndex}") { backStackEntry ->
                val forecastDay = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<com.example.agrohub.models.ForecastDay>("forecastDay")
                
                com.example.agrohub.ui.screens.weather.WeatherDetailScreen(
                    navController = navController,
                    forecastDay = forecastDay
                )
            }
            
            // Community Screen
            composable(Routes.COMMUNITY) {
                val context = androidx.compose.ui.platform.LocalContext.current
                
                // Create ViewModels with dependencies
                val feedRepository = com.example.agrohub.data.repository.FeedRepositoryImpl(
                    com.example.agrohub.data.remote.NetworkModule.provideFeedApiService(context)
                )
                val likeRepository = com.example.agrohub.data.repository.LikeRepositoryImpl(
                    com.example.agrohub.data.remote.NetworkModule.provideLikeApiService(context)
                )
                val postRepository = com.example.agrohub.data.repository.PostRepositoryImpl(
                    com.example.agrohub.data.remote.NetworkModule.providePostApiService(context)
                )
                val commentRepository = com.example.agrohub.data.repository.CommentRepositoryImpl(
                    com.example.agrohub.data.remote.NetworkModule.provideCommentApiService(context)
                )
                
                val feedViewModel = com.example.agrohub.presentation.feed.FeedViewModel(feedRepository, likeRepository)
                val postViewModel = com.example.agrohub.presentation.post.PostViewModel(postRepository, commentRepository)
                
                CommunityScreen(
                    navController = navController,
                    feedViewModel = feedViewModel,
                    postViewModel = postViewModel
                )
            }
            
            // Create Post Screen
            composable(Routes.CREATE_POST) {
                val context = androidx.compose.ui.platform.LocalContext.current
                
                val postRepository = com.example.agrohub.data.repository.PostRepositoryImpl(
                    com.example.agrohub.data.remote.NetworkModule.providePostApiService(context)
                )
                val commentRepository = com.example.agrohub.data.repository.CommentRepositoryImpl(
                    com.example.agrohub.data.remote.NetworkModule.provideCommentApiService(context)
                )
                
                val postViewModel = com.example.agrohub.presentation.post.PostViewModel(postRepository, commentRepository)
                
                com.example.agrohub.ui.screens.community.CreatePostScreen(
                    navController = navController,
                    viewModel = postViewModel
                )
            }
            
            // Farm/Field Map Screen
            composable(Routes.FARM) {
                FieldMapScreen(navController = navController)
            }
            
            // Marketplace Screen
            composable(Routes.MARKET) {
                MarketplaceScreen(navController = navController)
            }
            
            // Chat Screen (Agri-Bot)
            composable(Routes.CHAT) {
                ChatScreen()
            }
            
            // Add Farm Screen
            composable(Routes.ADD_FARM) {
                AddFarmScreen(navController = navController)
            }
            
            // Profile Screen
            composable(Routes.PROFILE) {
                ProfileScreen(navController = navController)
            }
            
            // Product Detail Screen (with parameter)
            composable(Routes.PRODUCT_DETAIL) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    navController = navController,
                    productId = productId
                )
            }
        }
    }
}
