package com.example.agrohub.ui.screens.backend

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.util.UiState
import com.example.agrohub.presentation.profile.ProfileViewModel
import java.time.format.DateTimeFormatter

/**
 * Profile screen demonstrating integration with ProfileViewModel.
 * Displays user information, follow stats, and follow/unfollow button.
 *
 * @param viewModel The ProfileViewModel managing profile state
 * @param userId ID of the user to display
 * @param onBackClick Callback invoked when back button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    userId: Long,
    onBackClick: () -> Unit = {}
) {
    val profileState by viewModel.profileState.collectAsState()
    val followStatsState by viewModel.followStatsState.collectAsState()
    
    // Load profile and stats on first composition
    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
        viewModel.loadFollowStats(userId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = profileState) {
                is UiState.Idle -> {
                    // Initial state
                }
                
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is UiState.Success -> {
                    ProfileContent(
                        user = state.data,
                        followStatsState = followStatsState,
                        onFollowClick = { viewModel.followUser(userId) },
                        onUnfollowClick = { viewModel.unfollowUser(userId) }
                    )
                }
                
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Button(onClick = { viewModel.loadProfile(userId) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Profile content displaying user information and stats.
 */
@Composable
private fun ProfileContent(
    user: User,
    followStatsState: UiState<com.example.agrohub.domain.model.FollowStats>,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit
) {
    var isFollowing by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // User info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (user.name.isNotBlank()) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                if (user.bio.isNotBlank()) {
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                if (user.location.isNotBlank()) {
                    Text(
                        text = "📍 ${user.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                user.website?.let { website ->
                    Text(
                        text = "🔗 $website",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Text(
                    text = "Joined ${user.createdAt.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // Follow stats
        when (val statsState = followStatsState) {
            is UiState.Success -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${statsState.data.followersCount}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Followers",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${statsState.data.followingCount}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Following",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
            else -> {
                // Don't show anything for idle or error states
            }
        }
        
        // Follow/Unfollow button
        Button(
            onClick = {
                if (isFollowing) {
                    onUnfollowClick()
                    isFollowing = false
                } else {
                    onFollowClick()
                    isFollowing = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(if (isFollowing) "Unfollow" else "Follow")
        }
    }
}
