package com.example.agrohub.ui.screens.disease

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.agrohub.ui.components.buttons.PrimaryButton
import com.example.agrohub.ui.components.buttons.SecondaryButton
import com.example.agrohub.ui.navigation.Routes
import com.example.agrohub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)

/**
 * Disease Detection Screen
 * Provides camera upload interface for crop disease detection
 * 
 * Requirements: 3.1, 3.2
 * 
 * @param navController Navigation controller for screen transitions
 */
@Composable
fun DiseaseDetectionScreen(
    navController: NavController
) {
    // State for selected image
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Image captured successfully
            // In a real app, we would use the URI we provided to TakePicture
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Disease Detection",
                        style = AgroHubTypography.Heading2,
                        color = AgroHubColors.TextPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AgroHubColors.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AgroHubColors.BackgroundLight)
        ) {
            // Show upload section when no image is selected
            AnimatedVisibility(
                visible = selectedImageUri == null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                CameraUploadSection(
                    onCaptureClick = {
                        // In a real app, create a URI for the camera to save to
                        // For now, just open gallery as fallback
                        galleryLauncher.launch("image/*")
                    },
                    onGalleryClick = {
                        galleryLauncher.launch("image/*")
                    }
                )
            }
            
            // Show preview section when image is selected
            AnimatedVisibility(
                visible = selectedImageUri != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                ImagePreviewSection(
                    imageUri = selectedImageUri,
                    onConfirm = {
                        // Navigate to disease result screen
                        // In a real app, this would trigger analysis
                        navController.navigate(Routes.diseaseResult("sample"))
                    },
                    onCancel = {
                        selectedImageUri = null
                    }
                )
            }
        }
    }
}


/**
 * Camera Upload Section
 * Displays interface for capturing or selecting crop images
 * 
 * Requirements: 3.1
 * 
 * @param onCaptureClick Handler for camera capture button
 * @param onGalleryClick Handler for gallery selection button
 */
@Composable
fun CameraUploadSection(
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AgroHubSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration/Icon area
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AgroHubColors.LightGreen,
                            AgroHubColors.White
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Camera",
                modifier = Modifier.size(80.dp),
                tint = AgroHubColors.DeepGreen
            )
        }
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.xl))
        
        // Title
        Text(
            text = "Scan Your Crop",
            style = AgroHubTypography.Heading2,
            color = AgroHubColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
        
        // Description
        Text(
            text = "Take a photo or select an image from your gallery to detect crop diseases",
            style = AgroHubTypography.Body,
            color = AgroHubColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AgroHubSpacing.lg)
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.xl))
        
        // Capture button
        PrimaryButton(
            text = "Capture Photo",
            onClick = onCaptureClick,
            icon = Icons.Default.CameraAlt,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.md))
        
        // Gallery button
        SecondaryButton(
            text = "Choose from Gallery",
            onClick = onGalleryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
    }
}


/**
 * Image Preview Section
 * Displays selected image with crop and confirm buttons
 * 
 * Requirements: 3.2
 * 
 * @param imageUri URI of the selected image
 * @param onConfirm Handler for confirm button
 * @param onCancel Handler for cancel button
 */
@Composable
fun ImagePreviewSection(
    imageUri: Uri?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AgroHubSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Preview Image",
            style = AgroHubTypography.Heading2,
            color = AgroHubColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.md))
        
        // Description
        Text(
            text = "Review your image before analysis",
            style = AgroHubTypography.Body,
            color = AgroHubColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
        
        // Image preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = AgroHubShapes.large,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = AgroHubColors.White
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected crop image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(AgroHubShapes.large),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder if no image
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "No image",
                        modifier = Modifier.size(80.dp),
                        tint = AgroHubColors.TextHint
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.xl))
        
        // Action buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
        ) {
            // Cancel button
            SecondaryButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            )
            
            // Confirm button
            PrimaryButton(
                text = "Analyze",
                onClick = onConfirm,
                icon = Icons.Default.Check,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            )
        }
    }
}
