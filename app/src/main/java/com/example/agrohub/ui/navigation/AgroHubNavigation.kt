package com.example.agrohub.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.agrohub.ui.screens.addfarm.AddFarmScreen
import com.example.agrohub.ui.screens.chat.ChatScreen
import com.example.agrohub.ui.screens.community.CommunityScreen
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME
    
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
            startDestination = Routes.HOME,
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
            // Home Screen
            composable(Routes.HOME) {
                HomeScreen(navController = navController)
            }
            
            // Disease Detection/Scan Screen
            composable(Routes.SCAN) {
                DiseaseDetectionScreen(navController = navController)
            }
            
            // Weather Screen
            composable(Routes.WEATHER) {
                WeatherScreen(navController = navController)
            }
            
            // Community Screen
            composable(Routes.COMMUNITY) {
                CommunityScreen(navController = navController)
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
            
            // Disease Result Screen (with parameter)
            composable(Routes.DISEASE_RESULT) { backStackEntry ->
                val diseaseId = backStackEntry.arguments?.getString("diseaseId") ?: ""
                DiseaseResultScreen(
                    navController = navController,
                    diseaseId = diseaseId
                )
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
