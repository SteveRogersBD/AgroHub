package com.example.agrohub.ui.screens.disease

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.agrohub.data.MockDataProvider
import com.example.agrohub.models.DiseaseResult
import com.example.agrohub.models.Treatment
import com.example.agrohub.ui.components.cards.GradientCard
import com.example.agrohub.ui.theme.*

/**
 * Disease Result Screen
 * Displays comprehensive disease analysis results with visual layout
 * 
 * Requirements: 3.3, 3.4, 3.5, 3.6
 * 
 * @param navController Navigation controller for back navigation
 * @param diseaseId ID of the disease result to display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseResultScreen(
    navController: NavController,
    diseaseId: String
) {
    // In a real app, we would fetch the disease result by ID
    // For now, we'll use mock data
    val diseaseResult = remember { MockDataProvider.getRandomDiseaseResult() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Disease Analysis",
                        style = AgroHubTypography.Heading2,
                        color = AgroHubColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AgroHubColors.DeepGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgroHubColors.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AgroHubColors.BackgroundLight)
                .padding(paddingValues),
            contentPadding = PaddingValues(AgroHubSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
        ) {
            // Crop Image Section
            item {
                CropImageSection(diseaseResult)
            }
            
            // Disease Info Card
            item {
                DiseaseInfoCard(diseaseResult)
            }
            
            // Severity Bar
            item {
                SeverityBar(
                    severity = diseaseResult.severity,
                    severityLevel = diseaseResult.severityLevel
                )
            }
            
            // Treatment Section Header
            item {
                SectionHeader(title = "Treatment Options")
            }
            
            // Treatment Cards
            items(diseaseResult.treatments) { treatment ->
                TreatmentCard(treatment)
            }
            
            // Prevention Section Header
            item {
                SectionHeader(title = "Prevention Tips")
            }
            
            // Prevention Checklist
            item {
                PreventionChecklist(preventionTips = diseaseResult.preventionTips)
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
            }
        }
    }
}


/**
 * Crop Image Section
 * Displays the crop image at the top with rounded corners
 * 
 * Requirements: 3.3
 * 
 * @param diseaseResult Disease result data containing the image
 */
@Composable
fun CropImageSection(diseaseResult: DiseaseResult) {
    // Animation state
    var visible by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = tween(durationMillis = 400),
        label = "scale"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .alpha(alpha)
            .scale(scale),
        shape = AgroHubShapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = rememberAsyncImagePainter(diseaseResult.imageUri),
                contentDescription = "Crop image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(AgroHubShapes.large),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay at bottom for better text visibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
            
            // Disease name overlay
            Text(
                text = diseaseResult.diseaseName,
                style = AgroHubTypography.Heading1,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(AgroHubSpacing.md)
            )
        }
    }
}

/**
 * Disease Info Card
 * Displays disease description in a gradient card
 * 
 * Requirements: 3.3
 * 
 * @param diseaseResult Disease result data
 */
@Composable
fun DiseaseInfoCard(diseaseResult: DiseaseResult) {
    GradientCard(
        gradient = Brush.horizontalGradient(
            colors = listOf(
                AgroHubColors.LightGreen,
                AgroHubColors.White
            )
        ),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "About This Disease",
                style = AgroHubTypography.Heading3,
                color = AgroHubColors.DeepGreen,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            Text(
                text = diseaseResult.description,
                style = AgroHubTypography.Body,
                color = AgroHubColors.TextPrimary
            )
        }
    }
}

/**
 * Severity Bar
 * Displays color-coded severity indicator with bar
 * 
 * Requirements: 3.3
 * 
 * @param severity Severity value (0.0 to 1.0)
 * @param severityLevel Severity level text (Mild, Moderate, Severe)
 */
@Composable
fun SeverityBar(
    severity: Float,
    severityLevel: String
) {
    // Animation state
    var visible by remember { mutableStateOf(false) }
    
    val animatedSeverity by animateFloatAsState(
        targetValue = if (visible) severity else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "severity"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    // Determine color based on severity
    val severityColor = when {
        severity < 0.33f -> AgroHubColors.HealthyGreen
        severity < 0.67f -> AgroHubColors.GoldenHarvest
        else -> AgroHubColors.CriticalRed
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AgroHubShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = AgroHubColors.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Severity Level",
                    style = AgroHubTypography.Heading3,
                    color = AgroHubColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = severityLevel,
                    style = AgroHubTypography.Heading3,
                    color = severityColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // Severity bar background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AgroHubColors.BackgroundLight)
            ) {
                // Severity bar fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedSeverity)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    severityColor.copy(alpha = 0.7f),
                                    severityColor
                                )
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
            
            // Percentage text
            Text(
                text = "${(severity * 100).toInt()}% Severity",
                style = AgroHubTypography.Caption,
                color = AgroHubColors.TextSecondary
            )
        }
    }
}


/**
 * Section Header
 * Displays a section header with illustrated styling
 * 
 * Requirements: 3.6
 * 
 * @param title Section title text
 */
@Composable
fun SectionHeader(title: String) {
    // Animation state
    var visible by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .padding(vertical = AgroHubSpacing.sm)
    ) {
        Text(
            text = title,
            style = AgroHubTypography.Heading2,
            color = AgroHubColors.DeepGreen,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Treatment Card
 * Displays treatment information in expandable card format
 * 
 * Requirements: 3.4
 * 
 * @param treatment Treatment data with title, steps, and icon
 */
@Composable
fun TreatmentCard(treatment: Treatment) {
    var expanded by remember { mutableStateOf(false) }
    
    // Animation state
    var visible by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .scale(scale)
            .clickable { expanded = !expanded },
        shape = AgroHubShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = AgroHubColors.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.md)
        ) {
            // Header row with icon, title, and expand button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon with gradient background
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        AgroHubColors.LightGreen,
                                        AgroHubColors.MediumGreen
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = treatment.icon,
                            contentDescription = treatment.title,
                            tint = AgroHubColors.DeepGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(AgroHubSpacing.md))
                    
                    Text(
                        text = treatment.title,
                        style = AgroHubTypography.Heading3,
                        color = AgroHubColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Expand/collapse icon
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = AgroHubColors.DeepGreen
                )
            }
            
            // Expandable content with animation
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AgroHubSpacing.md)
                ) {
                    Divider(
                        color = AgroHubColors.BackgroundLight,
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = AgroHubSpacing.md)
                    )
                    
                    // Treatment steps
                    treatment.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AgroHubSpacing.xs),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Step number
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AgroHubColors.LightGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = AgroHubTypography.Caption,
                                    color = AgroHubColors.DeepGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(AgroHubSpacing.sm))
                            
                            // Step text
                            Text(
                                text = step,
                                style = AgroHubTypography.Body,
                                color = AgroHubColors.TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}


/**
 * Prevention Checklist
 * Displays prevention tips as checkable items in a checklist format
 * 
 * Requirements: 3.5
 * 
 * @param preventionTips List of prevention tip strings
 */
@Composable
fun PreventionChecklist(preventionTips: List<String>) {
    // Track checked state for each item
    val checkedStates = remember { mutableStateMapOf<Int, Boolean>() }
    
    // Animation state
    var visible by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .scale(scale),
        shape = AgroHubShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = AgroHubColors.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.md)
        ) {
            // Header with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AgroHubColors.LightGreen,
                                AgroHubColors.White
                            )
                        )
                    )
                    .padding(AgroHubSpacing.md)
            ) {
                Text(
                    text = "Follow these prevention tips to keep your crops healthy",
                    style = AgroHubTypography.Body,
                    color = AgroHubColors.DeepGreen,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // Prevention tips as checkable items
            preventionTips.forEachIndexed { index, tip ->
                val isChecked = checkedStates[index] ?: false
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { checkedStates[index] = !isChecked }
                        .padding(vertical = AgroHubSpacing.sm),
                    verticalAlignment = Alignment.Top
                ) {
                    // Checkbox
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isChecked) AgroHubColors.DeepGreen else AgroHubColors.BackgroundLight
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecked) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Checked",
                                tint = AgroHubColors.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(AgroHubSpacing.md))
                    
                    // Tip text
                    Text(
                        text = tip,
                        style = AgroHubTypography.Body,
                        color = if (isChecked) AgroHubColors.TextSecondary else AgroHubColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Divider between items (except last)
                if (index < preventionTips.size - 1) {
                    Divider(
                        color = AgroHubColors.BackgroundLight,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = AgroHubSpacing.xs)
                    )
                }
            }
        }
    }
}
