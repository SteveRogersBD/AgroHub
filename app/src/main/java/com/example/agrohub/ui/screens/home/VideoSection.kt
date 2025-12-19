package com.example.agrohub.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.agrohub.models.VideoResult
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

@Composable
fun VideoSection(videos: List<VideoResult>, isLoading: Boolean) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        // Section header with YouTube-style red accent
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(4.dp, 24.dp),
                color = Color(0xFFFF0000), // YouTube red
                shape = RoundedCornerShape(2.dp)
            ) {}
            
            Text(
                text = "Agriculture Videos",
                style = AgroHubTypography.Heading3,
                color = AgroHubColors.TextPrimary
            )
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AgroHubColors.DeepGreen)
            }
        } else if (videos.isEmpty()) {
            Text(
                text = "No videos available",
                style = AgroHubTypography.Body,
                color = AgroHubColors.TextSecondary
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(videos) { video ->
                    VideoCard(
                        video = video,
                        onClick = {
                            video.link?.let { link ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VideoCard(video: VideoResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(320.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Thumbnail with duration overlay and red play indicator
            Box {
                AsyncImage(
                    model = video.thumbnail?.mystatic ?: video.thumbnail?.rich,
                    contentDescription = video.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
                
                // Red play button overlay (center)
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp),
                    color = Color(0xFFFF0000).copy(alpha = 0.9f), // YouTube red
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Play triangle
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.size(20.dp)
                        ) {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(0f, 0f)
                                lineTo(size.width, size.height / 2)
                                lineTo(0f, size.height)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = Color.White
                            )
                        }
                    }
                }
                
                // Duration badge (bottom right)
                video.length?.let { length ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = length,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = AgroHubTypography.Caption.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
            
            // Video info section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Channel thumbnail (circular)
                video.channel?.thumbnail?.let { channelThumb ->
                    AsyncImage(
                        model = channelThumb,
                        contentDescription = video.channel.name,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title
                    Text(
                        text = video.title ?: "Untitled",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        ),
                        color = Color(0xFF0F0F0F),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Channel name
                    video.channel?.name?.let { channelName ->
                        Text(
                            text = channelName,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = Color(0xFF606060),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Views and published date
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        video.views?.let { views ->
                            Text(
                                text = formatViews(views),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = Color(0xFF606060)
                            )
                        }
                        
                        if (video.views != null && video.publishedDate != null) {
                            Text(
                                text = "•",
                                style = TextStyle(fontSize = 12.sp),
                                color = Color(0xFF606060)
                            )
                        }
                        
                        video.publishedDate?.let { date ->
                            Text(
                                text = date,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = Color(0xFF606060)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatViews(views: Int): String {
    return when {
        views >= 1_000_000 -> "${views / 1_000_000}M views"
        views >= 1_000 -> "${views / 1_000}K views"
        else -> "$views views"
    }
}
