package com.example.agrohub.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.agrohub.data.MockDataProvider
import com.example.agrohub.data.remote.NetworkModule
import com.example.agrohub.data.repository.CommentRepositoryImpl
import com.example.agrohub.data.repository.FeedRepositoryImpl
import com.example.agrohub.data.repository.LikeRepositoryImpl
import com.example.agrohub.data.repository.PostRepositoryImpl
import com.example.agrohub.presentation.feed.FeedViewModel
import com.example.agrohub.presentation.post.PostViewModel
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.navigation.Routes
import com.example.agrohub.ui.screens.community.CommunityScreen
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * HomeScreen - Main dashboard screen with tabs for Home and Community
 * 
 * Features:
 * - Tab navigation between Home and Community
 * - Scrollable layout
 * - Fade-in animations
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6
 * 
 * @param navController Navigation controller for screen navigation
 */
@Composable
fun HomeScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Community")
    val context = LocalContext.current
    
    // Create ViewModels for Community tab
    val feedViewModel = remember {
        val feedRepository = FeedRepositoryImpl(
            NetworkModule.provideFeedApiService(context)
        )
        val likeRepository = LikeRepositoryImpl(
            NetworkModule.provideLikeApiService(context)
        )
        FeedViewModel(feedRepository, likeRepository)
    }
    
    val postViewModel = remember {
        val postRepository = PostRepositoryImpl(
            NetworkModule.providePostApiService(context)
        )
        val commentRepository = CommentRepositoryImpl(
            NetworkModule.provideCommentApiService(context)
        )
        PostViewModel(postRepository, commentRepository)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = AgroHubColors.White,
                contentColor = AgroHubColors.DeepGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = AgroHubColors.DeepGreen
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = AgroHubTypography.Heading3,
                                color = if (selectedTabIndex == index) 
                                    AgroHubColors.DeepGreen 
                                else 
                                    AgroHubColors.CharcoalText.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }
            
            // Tab Content
            when (selectedTabIndex) {
                0 -> HomeTabContent(navController = navController)
                1 -> CommunityScreen(
                    navController = navController,
                    feedViewModel = feedViewModel,
                    postViewModel = postViewModel
                )
            }
        }
        
        // Floating Chatbot Button
        FloatingActionButton(
            onClick = { navController.navigate(Routes.CHAT) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = CircleShape,
            containerColor = AgroHubColors.DeepGreen,
            contentColor = AgroHubColors.White
        ) {
            Icon(
                imageVector = AgroHubIcons.Chat,
                contentDescription = "Agriculture Assistant",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * HomeTabContent - Content for the Home tab
 * 
 * Displays:
 * - Dashboard header with greeting and notifications
 * - Quick statistics cards
 * - Farm snapshot with crop status cards
 * - Weather warning card
 * - Quick action buttons
 * - Map preview
 */
@Composable
private fun HomeTabContent(navController: NavController) {
    // Load mock data
    val farms = remember { MockDataProvider.generateFarmData() }
    val quickStats = remember { MockDataProvider.generateQuickStats() }
    val weatherForecast = remember { MockDataProvider.generateWeatherForecast() }
    
    // Animation state for fade-in
    var visible by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "screen_alpha"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha),
        contentPadding = PaddingValues(AgroHubSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.lg)
    ) {
        // Dashboard Header
        item {
            DashboardHeader(
                userName = "Farmer",
                notificationCount = 3
            )
        }
        
        // Quick Stats Section
        item {
            QuickStatsSection(
                totalFarms = quickStats.totalFarms,
                activeCrops = quickStats.activeCrops,
                pendingTasks = quickStats.pendingTasks
            )
        }
        
        // Farm Snapshot Section
        item {
            FarmSnapshotSection(farms = farms)
        }
        
        // Weather Warning Card
        item {
            val todayForecast = weatherForecast.firstOrNull()
            if (todayForecast != null) {
                WeatherWarningCard(
                    temperature = todayForecast.tempHigh,
                    condition = todayForecast.condition,
                    riskLevel = todayForecast.riskLevel
                )
            }
        }
        
        // Quick Actions Grid
        item {
            QuickActionsGrid(navController = navController)
        }
        
        // Map Preview
        item {
            MapPreview(farmLocations = farms.map { it.location })
        }
    }
}
