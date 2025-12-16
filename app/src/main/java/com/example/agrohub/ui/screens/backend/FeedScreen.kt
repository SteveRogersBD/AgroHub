package com.example.agrohub.ui.screens.backend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agrohub.domain.model.FeedPost
import com.example.agrohub.domain.util.UiState
import com.example.agrohub.presentation.feed.FeedViewModel
import java.time.format.DateTimeFormatter

/**
 * Feed screen demonstrating integration with FeedViewModel.
 * Displays a list of posts with pull-to-refresh and pagination support.
 *
 * @param viewModel The FeedViewModel managing feed state
 * @param onPostClick Callback invoked when a post is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onPostClick: (Long) -> Unit = {}
) {
    val feedState by viewModel.feedState.collectAsState()
    val listState = rememberLazyListState()
    
    // Load feed on first composition
    LaunchedEffect(Unit) {
        if (feedState is UiState.Idle) {
            viewModel.loadFeed(refresh = true)
        }
    }
    
    // Detect when user scrolls to bottom for pagination
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val feedData = (feedState as? UiState.Success)?.data
                if (feedData != null && lastVisibleIndex != null) {
                    val totalItems = feedData.items.size
                    // Load more when user is 3 items from the end
                    if (lastVisibleIndex >= totalItems - 3 && !feedData.isLastPage) {
                        viewModel.loadFeed(refresh = false)
                    }
                }
            }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feed") }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = feedState is UiState.Loading,
            onRefresh = { viewModel.loadFeed(refresh = true) },
            modifier = Modifier.padding(paddingValues)
        ) {
            when (val state = feedState) {
                is UiState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Pull to refresh")
                    }
                }
                
                is UiState.Loading -> {
                    if ((state as? UiState.Success)?.data?.items?.isEmpty() != false) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                
                is UiState.Success -> {
                    val posts = state.data.items
                    if (posts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No posts yet")
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(posts, key = { it.id }) { post ->
                                FeedPostItem(
                                    post = post,
                                    onLikeClick = {
                                        if (post.isLikedByCurrentUser) {
                                            viewModel.unlikePost(post.id)
                                        } else {
                                            viewModel.likePost(post.id)
                                        }
                                    },
                                    onPostClick = { onPostClick(post.id) }
                                )
                            }
                            
                            // Loading indicator at bottom during pagination
                            if (!state.data.isLastPage) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
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
                            Button(onClick = { viewModel.loadFeed(refresh = true) }) {
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
 * Individual post item in the feed.
 */
@Composable
private fun FeedPostItem(
    post: FeedPost,
    onLikeClick: () -> Unit,
    onPostClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onPostClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Author info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.author.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = post.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            // Like and comment counts
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (post.isLikedByCurrentUser) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        contentDescription = if (post.isLikedByCurrentUser) "Unlike" else "Like",
                        tint = if (post.isLikedByCurrentUser) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                Text(
                    text = "${post.likeCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "${post.commentCount} comments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
