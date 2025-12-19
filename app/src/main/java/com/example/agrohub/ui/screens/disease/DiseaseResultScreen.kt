package com.example.agrohub.ui.screens.disease

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.agrohub.models.DiseaseDetectionResult
import com.example.agrohub.models.NewsResult
import com.example.agrohub.presentation.disease.DiseaseDetectionUiState
import com.example.agrohub.presentation.disease.DiseaseDetectionViewModel
import com.example.agrohub.presentation.disease.NewsState
import com.example.agrohub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseResultScreen(
    navController: NavController,
    viewModel: DiseaseDetectionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val newsState by viewModel.newsState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disease Analysis") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.reset()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
            when (uiState) {
                is DiseaseDetectionUiState.Loading -> {
                    LoadingView()
                }
                is DiseaseDetectionUiState.Success -> {
                    val result = (uiState as DiseaseDetectionUiState.Success).result
                    DiseaseResultContent(result, newsState)
                }
                is DiseaseDetectionUiState.Error -> {
                    ErrorView(
                        message = (uiState as DiseaseDetectionUiState.Error).message,
                        onRetry = { navController.popBackStack() }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = AgroHubColors.DeepGreen
        )
        Spacer(modifier = Modifier.height(AgroHubSpacing.md))
        Text(
            text = "Analyzing crop disease...",
            style = AgroHubTypography.Body,
            color = AgroHubColors.TextSecondary
        )
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AgroHubSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = AgroHubColors.CriticalRed
        )
        Spacer(modifier = Modifier.height(AgroHubSpacing.md))
        Text(
            text = "Analysis Failed",
            style = AgroHubTypography.Heading2,
            color = AgroHubColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
        Text(
            text = message,
            style = AgroHubTypography.Body,
            color = AgroHubColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = AgroHubColors.DeepGreen
            )
        ) {
            Text("Try Again")
        }
    }
}

@Composable
fun DiseaseResultContent(result: DiseaseDetectionResult, newsState: NewsState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AgroHubSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.lg)
    ) {
        // Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            shape = AgroHubShapes.large,
            colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
        ) {
            Image(
                painter = rememberAsyncImagePainter(result.imageUri),
                contentDescription = "Crop image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Disease Name
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AgroHubShapes.medium,
            colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
        ) {
            Column(
                modifier = Modifier.padding(AgroHubSpacing.md)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = AgroHubColors.CriticalRed,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = result.diseaseName,
                        style = AgroHubTypography.Heading2,
                        color = AgroHubColors.TextPrimary
                    )
                }
            }
        }
        
        // Description
        InfoSection(
            title = "Description",
            icon = Icons.Default.Info,
            content = result.description
        )
        
        // Symptoms
        InfoListSection(
            title = "Symptoms",
            icon = Icons.Default.Warning,
            items = result.symptoms
        )
        
        // Potential Threats
        InfoListSection(
            title = "Potential Threats",
            icon = Icons.Default.Dangerous,
            items = result.potentialThreats
        )
        
        // Prevention
        InfoListSection(
            title = "Prevention",
            icon = Icons.Default.Shield,
            items = result.prevention
        )
        
        // Treatment
        InfoListSection(
            title = "Treatment",
            icon = Icons.Default.MedicalServices,
            items = result.treatment
        )
        
        // Post Disease Management
        InfoListSection(
            title = "Post Disease Management",
            icon = Icons.Default.CheckCircle,
            items = result.postDiseaseManagement
        )
        
        // Related Articles
        RelatedArticlesSection(newsState)
    }
}

@Composable
fun InfoSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AgroHubShapes.medium,
        colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
    ) {
        Column(
            modifier = Modifier.padding(AgroHubSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AgroHubColors.DeepGreen,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = AgroHubTypography.Heading3,
                    color = AgroHubColors.TextPrimary
                )
            }
            Text(
                text = content,
                style = AgroHubTypography.Body,
                color = AgroHubColors.TextSecondary
            )
        }
    }
}

@Composable
fun InfoListSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    items: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AgroHubShapes.medium,
        colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
    ) {
        Column(
            modifier = Modifier.padding(AgroHubSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AgroHubColors.DeepGreen,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = AgroHubTypography.Heading3,
                    color = AgroHubColors.TextPrimary
                )
            }
            
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
                ) {
                    Text(
                        text = "•",
                        style = AgroHubTypography.Body,
                        color = AgroHubColors.DeepGreen
                    )
                    Text(
                        text = item,
                        style = AgroHubTypography.Body,
                        color = AgroHubColors.TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun RelatedArticlesSection(newsState: NewsState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        Text(
            text = "Related Articles",
            style = AgroHubTypography.Heading2,
            color = AgroHubColors.TextPrimary
        )
        
        when (newsState) {
            is NewsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AgroHubColors.DeepGreen)
                }
            }
            is NewsState.Success -> {
                if (newsState.articles.isEmpty()) {
                    Text(
                        text = "No related articles found",
                        style = AgroHubTypography.Body,
                        color = AgroHubColors.TextSecondary
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
                    ) {
                        items(newsState.articles) { article ->
                            NewsArticleCard(article)
                        }
                    }
                }
            }
            is NewsState.Error -> {
                Text(
                    text = "Failed to load articles",
                    style = AgroHubTypography.Body,
                    color = AgroHubColors.CriticalRed
                )
            }
        }
    }
}

@Composable
fun NewsArticleCard(article: NewsResult) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(350.dp)
            .clickable {
                article.link?.let { link ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                }
            },
        shape = AgroHubShapes.medium,
        colors = CardDefaults.cardColors(containerColor = AgroHubColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Thumbnail
            article.thumbnail?.let { thumbnail ->
                AsyncImage(
                    model = thumbnail,
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AgroHubSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
            ) {
                // Title
                Text(
                    text = article.title ?: "Untitled",
                    style = AgroHubTypography.Body.copy(fontWeight = FontWeight.Bold),
                    color = AgroHubColors.TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Source and date
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    article.source?.name?.let { sourceName ->
                        Text(
                            text = sourceName,
                            style = AgroHubTypography.Caption,
                            color = AgroHubColors.DeepGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    article.date?.let { date ->
                        Text(
                            text = "• $date",
                            style = AgroHubTypography.Caption,
                            color = AgroHubColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
