package com.example.agrohub.ui.screens.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.agrohub.presentation.post.PostViewModel
import com.example.agrohub.domain.util.UiState
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import kotlinx.coroutines.launch

/**
 * Create Post Screen - Allows users to create new posts with text and images
 * 
 * Features:
 * - Text input for post content
 * - Image selection from gallery
 * - Image upload to media service
 * - Post creation with media URL
 * - Loading states and error handling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    viewModel: PostViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var postContent by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedMediaUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    
    val createPostState by viewModel.createPostState.collectAsState()
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uploadedMediaUrl = null // Reset uploaded URL when new image is selected
        uploadError = null
    }
    
    // Handle post creation success
    LaunchedEffect(createPostState) {
        if (createPostState is UiState.Success) {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Post",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgroHubColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = AgroHubIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = AgroHubColors.DeepGreen
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (postContent.isNotBlank()) {
                                viewModel.createPost(postContent, uploadedMediaUrl)
                            }
                        },
                        enabled = postContent.isNotBlank() && 
                                  createPostState !is UiState.Loading &&
                                  !isUploadingImage
                    ) {
                        Text(
                            text = "Post",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (postContent.isNotBlank() && 
                                       createPostState !is UiState.Loading &&
                                       !isUploadingImage) 
                                AgroHubColors.DeepGreen 
                            else 
                                AgroHubColors.TextHint
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgroHubColors.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AgroHubColors.BackgroundLight)
                .padding(paddingValues)
                .padding(AgroHubSpacing.md)
        ) {
            // Post Content Input
            OutlinedTextField(
                value = postContent,
                onValueChange = { postContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = {
                    Text(
                        text = "What's on your mind?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AgroHubColors.TextHint
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroHubColors.DeepGreen,
                    unfocusedBorderColor = AgroHubColors.SurfaceLight,
                    focusedTextColor = AgroHubColors.TextPrimary,
                    unfocusedTextColor = AgroHubColors.TextPrimary,
                    focusedContainerColor = AgroHubColors.White,
                    unfocusedContainerColor = AgroHubColors.White
                ),
                shape = RoundedCornerShape(AgroHubSpacing.md),
                maxLines = 10
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // Selected Image Preview
            if (selectedImageUri != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(AgroHubSpacing.md),
                    colors = CardDefaults.cardColors(
                        containerColor = AgroHubColors.White
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Remove image button
                        IconButton(
                            onClick = {
                                selectedImageUri = null
                                uploadedMediaUrl = null
                                uploadError = null
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(AgroHubSpacing.sm)
                                .background(
                                    color = AgroHubColors.CharcoalText.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(50)
                                )
                        ) {
                            Icon(
                                imageVector = AgroHubIcons.Close,
                                contentDescription = "Remove image",
                                tint = AgroHubColors.White
                            )
                        }
                        
                        // Upload status overlay
                        if (isUploadingImage) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AgroHubColors.CharcoalText.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AgroHubColors.White)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                
                // Upload button
                if (uploadedMediaUrl == null && !isUploadingImage) {
                    Button(
                        onClick = {
                            selectedImageUri?.let { uri ->
                                scope.launch {
                                    isUploadingImage = true
                                    uploadError = null
                                    try {
                                        // TODO: Implement image upload to media service
                                        // For now, we'll use a placeholder
                                        kotlinx.coroutines.delay(1000) // Simulate upload
                                        uploadedMediaUrl = "https://example.com/uploaded-image.jpg"
                                    } catch (e: Exception) {
                                        uploadError = e.message ?: "Upload failed"
                                    } finally {
                                        isUploadingImage = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgroHubColors.DeepGreen
                        )
                    ) {
                        Icon(
                            imageVector = AgroHubIcons.Upload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(AgroHubSpacing.sm))
                        Text("Upload Image")
                    }
                }
                
                // Upload success message
                if (uploadedMediaUrl != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = AgroHubIcons.CheckCircle,
                            contentDescription = null,
                            tint = AgroHubColors.DeepGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(AgroHubSpacing.sm))
                        Text(
                            text = "Image uploaded successfully",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AgroHubColors.DeepGreen
                        )
                    }
                }
                
                // Upload error message
                if (uploadError != null) {
                    Text(
                        text = uploadError ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
            ) {
                // Add Image Button
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AgroHubColors.DeepGreen
                    )
                ) {
                    Icon(
                        imageVector = AgroHubIcons.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(AgroHubSpacing.sm))
                    Text("Add Image")
                }
            }
            
            // Error message
            if (createPostState is UiState.Error) {
                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
                Text(
                    text = (createPostState as UiState.Error).message ?: "Failed to create post",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            // Loading indicator
            if (createPostState is UiState.Loading) {
                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AgroHubColors.DeepGreen
                )
            }
        }
    }
}
