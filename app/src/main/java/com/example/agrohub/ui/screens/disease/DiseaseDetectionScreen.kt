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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.agrohub.models.*
import com.example.agrohub.presentation.disease.DiseaseDetectionViewModel
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
    val context = LocalContext.current
    val viewModel = remember { 
        com.example.agrohub.presentation.disease.DiseaseDetectionViewModelFactory.getInstance(context)
    }
    
    // State for selected image
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showInputForm by remember { mutableStateOf(false) }
    
    // Observe UI state for navigation
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState) {
        if (uiState is com.example.agrohub.presentation.disease.DiseaseDetectionUiState.Success ||
            uiState is com.example.agrohub.presentation.disease.DiseaseDetectionUiState.Loading) {
            navController.navigate("disease_result_screen")
        }
    }
    
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
        if (uri != null) {
            showInputForm = true
        }
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
                visible = selectedImageUri == null && !showInputForm,
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
            
            // Show input form when image is selected
            AnimatedVisibility(
                visible = showInputForm && selectedImageUri != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                DiseaseInputForm(
                    imageUri = selectedImageUri,
                    onSubmit = { input ->
                        viewModel.analyzeDisease(input)
                        showInputForm = false
                    },
                    onCancel = {
                        selectedImageUri = null
                        showInputForm = false
                    }
                )
            }
        }
    }
}


/**
 * Disease Input Form
 * Form to collect crop information before analysis
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseInputForm(
    imageUri: Uri?,
    onSubmit: (DiseaseDetectionInput) -> Unit,
    onCancel: () -> Unit
) {
    var cropName by remember { mutableStateOf("") }
    var selectedCropType by remember { mutableStateOf(CropType.VEGETABLE) }
    var selectedGrowthStage by remember { mutableStateOf(GrowthStage.VEGETATIVE) }
    var selectedAffectedArea by remember { mutableStateOf(AffectedArea.LEAVES) }
    var symptoms by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }
    
    var showCropTypeDropdown by remember { mutableStateOf(false) }
    var showGrowthStageDropdown by remember { mutableStateOf(false) }
    var showAffectedAreaDropdown by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AgroHubSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        // Image preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = AgroHubShapes.medium,
            colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected crop image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        
        Text(
            text = "Crop Information",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.TextPrimary
        )
        
        // Crop Name
        OutlinedTextField(
            value = cropName,
            onValueChange = { cropName = it },
            label = { Text("Crop Name") },
            placeholder = { Text("e.g., Tomato, Wheat, Rice") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgroHubColors.DeepGreen,
                focusedLabelColor = AgroHubColors.DeepGreen
            )
        )
        
        // Crop Type Dropdown
        ExposedDropdownMenuBox(
            expanded = showCropTypeDropdown,
            onExpandedChange = { showCropTypeDropdown = it }
        ) {
            OutlinedTextField(
                value = selectedCropType.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Crop Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCropTypeDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroHubColors.DeepGreen,
                    focusedLabelColor = AgroHubColors.DeepGreen
                )
            )
            ExposedDropdownMenu(
                expanded = showCropTypeDropdown,
                onDismissRequest = { showCropTypeDropdown = false }
            ) {
                CropType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        onClick = {
                            selectedCropType = type
                            showCropTypeDropdown = false
                        }
                    )
                }
            }
        }
        
        // Growth Stage Dropdown
        ExposedDropdownMenuBox(
            expanded = showGrowthStageDropdown,
            onExpandedChange = { showGrowthStageDropdown = it }
        ) {
            OutlinedTextField(
                value = selectedGrowthStage.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Growth Stage") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGrowthStageDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroHubColors.DeepGreen,
                    focusedLabelColor = AgroHubColors.DeepGreen
                )
            )
            ExposedDropdownMenu(
                expanded = showGrowthStageDropdown,
                onDismissRequest = { showGrowthStageDropdown = false }
            ) {
                GrowthStage.values().forEach { stage ->
                    DropdownMenuItem(
                        text = { Text(stage.displayName) },
                        onClick = {
                            selectedGrowthStage = stage
                            showGrowthStageDropdown = false
                        }
                    )
                }
            }
        }
        
        // Affected Area Dropdown
        ExposedDropdownMenuBox(
            expanded = showAffectedAreaDropdown,
            onExpandedChange = { showAffectedAreaDropdown = it }
        ) {
            OutlinedTextField(
                value = selectedAffectedArea.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Affected Area") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAffectedAreaDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroHubColors.DeepGreen,
                    focusedLabelColor = AgroHubColors.DeepGreen
                )
            )
            ExposedDropdownMenu(
                expanded = showAffectedAreaDropdown,
                onDismissRequest = { showAffectedAreaDropdown = false }
            ) {
                AffectedArea.values().forEach { area ->
                    DropdownMenuItem(
                        text = { Text(area.displayName) },
                        onClick = {
                            selectedAffectedArea = area
                            showAffectedAreaDropdown = false
                        }
                    )
                }
            }
        }
        
        // Symptoms
        OutlinedTextField(
            value = symptoms,
            onValueChange = { symptoms = it },
            label = { Text("Symptoms") },
            placeholder = { Text("Describe what you observe...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgroHubColors.DeepGreen,
                focusedLabelColor = AgroHubColors.DeepGreen
            )
        )
        
        // Additional Info
        OutlinedTextField(
            value = additionalInfo,
            onValueChange = { additionalInfo = it },
            label = { Text("Additional Information (Optional)") },
            placeholder = { Text("Any other relevant details...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgroHubColors.DeepGreen,
                focusedLabelColor = AgroHubColors.DeepGreen
            )
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.md))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
        ) {
            SecondaryButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            )
            
            PrimaryButton(
                text = "Analyze",
                onClick = {
                    if (imageUri != null && cropName.isNotBlank() && symptoms.isNotBlank()) {
                        val input = DiseaseDetectionInput(
                            imageUri = imageUri,
                            cropName = cropName,
                            cropType = selectedCropType,
                            growthStage = selectedGrowthStage,
                            affectedArea = selectedAffectedArea,
                            symptoms = symptoms,
                            additionalInfo = additionalInfo
                        )
                        onSubmit(input)
                    }
                },
                icon = Icons.Default.Search,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = cropName.isNotBlank() && symptoms.isNotBlank()
            )
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
