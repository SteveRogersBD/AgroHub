package com.example.agrohub.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.agrohub.models.NewsResult
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

@Composable
fun NewsSection(news: List<NewsResult>, isLoading: Boolean) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        Text(
            text = "Agriculture News",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.TextPrimary
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AgroHubColors.DeepGreen)
            }
        } else if (news.isEmpty()) {
            Text(
                text = "No news available",
                style = AgroHubTypography.Body,
                color = AgroHubColors.TextSecondary
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                news.forEach { newsItem ->
                    NewsCard(
                        news = newsItem,
                        onClick = {
                            newsItem.link?.let { link ->
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
fun NewsCard(news: NewsResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AgroHubColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail
            news.thumbnail?.let { thumbnail ->
                AsyncImage(
                    model = thumbnail,
                    contentDescription = news.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title
                Text(
                    text = news.title ?: "Untitled",
                    style = AgroHubTypography.Body.copy(fontWeight = FontWeight.SemiBold),
                    color = AgroHubColors.TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Source and date
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    news.source?.name?.let { sourceName ->
                        Text(
                            text = sourceName,
                            style = AgroHubTypography.Caption,
                            color = AgroHubColors.DeepGreen
                        )
                    }
                    news.date?.let { date ->
                        Text(
                            text = "• $date",
                            style = AgroHubTypography.Caption,
                            color = AgroHubColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}
