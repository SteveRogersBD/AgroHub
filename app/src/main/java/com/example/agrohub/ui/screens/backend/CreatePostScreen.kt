package com.example.agrohub.ui.screens.backend

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agrohub.domain.util.UiState
import com.example.agrohub.presentation.post.PostViewModel

/**
 * Create post screen demonstrating integration with PostViewModel.
 * Displays content input field with success/error feedback.
 *
 * @param viewModel The PostViewModel managing post creation state
 * @param onPostCreated Callback invoked when post is successfully created
 * @param onBackClick Callback invoked when back button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: PostViewModel,
    onPostCreated: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var content by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("") }
    val createPostState by viewModel.createPostState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle successful post creation
    LaunchedEffect(createPostState) {
        when (val state = createPostState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Post created successfully!",
                    duration = SnackbarDuration.Short
                )
                // Clear fields
                content = ""
                mediaUrl = ""
                onPostCreated()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> { /* No action needed */ }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (content.isNotBlank()) {
                                viewModel.createPost(
                                    content = content,
                                    mediaUrl = mediaUrl.ifBlank { null }
                                )
                            }
                        },
                        enabled = createPostState !is UiState.Loading && content.isNotBlank()
                    ) {
                        if (createPostState is UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Post")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Content input
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp),
                enabled = createPostState !is UiState.Loading,
                maxLines = 10
            )
            
            // Media URL input (optional)
            OutlinedTextField(
                value = mediaUrl,
                onValueChange = { mediaUrl = it },
                label = { Text("Media URL (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = createPostState !is UiState.Loading,
                singleLine = true,
                placeholder = { Text("https://example.com/image.jpg") }
            )
            
            // Character count
            Text(
                text = "${content.length} characters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Create post button
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        viewModel.createPost(
                            content = content,
                            mediaUrl = mediaUrl.ifBlank { null }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = createPostState !is UiState.Loading && content.isNotBlank()
            ) {
                if (createPostState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Post")
                }
            }
        }
    }
}
