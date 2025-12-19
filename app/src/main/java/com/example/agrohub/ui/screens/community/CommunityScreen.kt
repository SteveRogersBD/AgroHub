package com.example.agrohub.ui.screens.community

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.agrohub.data.MockDataProvider
import com.example.agrohub.domain.model.FeedPost
import com.example.agrohub.domain.model.Comment
import com.example.agrohub.domain.util.UiState
import com.example.agrohub.presentation.feed.FeedViewModel
import com.example.agrohub.presentation.post.PostViewModel
import com.example.agrohub.ui.components.buttons.AgroHubFAB
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.navigation.Routes
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import java.time.format.DateTimeFormatter

/**
 * Community Screen - Social feed for farmer interactions
 * 
 * Features:
 * - Scrollable feed with posts from followed users
 * - Post cards with user info, content, images
 * - Like, comment, share interactions
 * - Comment sections with nested threads
 * - Create post button
 * - Real-time data from backend APIs
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6
 */
@Composable
fun CommunityScreen(
    navController: NavController,
    feedViewModel: FeedViewModel,
    postViewModel: PostViewModel
) {
    // Use mock data for demo
    val mockPosts = remember { MockDataProvider.generateMockFeedPosts() }
    var expandedPostId by remember { mutableStateOf<Long?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroHubColors.BackgroundLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Create Post Button
            CommunityHeader(
                onCreatePostClick = { navController.navigate(Routes.CreatePost.route) }
            )
            
            // Community Feed with Mock Data
            if (mockPosts.isEmpty()) {
                EmptyFeedMessage()
            } else {
                CommunityFeed(
                    posts = mockPosts,
                    expandedPostId = expandedPostId,
                    onPostExpand = { postId ->
                        expandedPostId = if (expandedPostId == postId) null else postId
                    },
                    onLike = { postId -> 
                        // Mock like action - just for demo
                    },
                    onComment = { postId -> expandedPostId = postId },
                    onLoadComments = { postId -> 
                        // Mock load comments - handled in FeedPostCard
                    },
                    postViewModel = postViewModel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Community Header with Create Post Button
 * Requirements: 6.1
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHeader(onCreatePostClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Community",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AgroHubColors.TextPrimary
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AgroHubColors.White
        ),
        actions = {
            IconButton(onClick = onCreatePostClick) {
                Icon(
                    imageVector = AgroHubIcons.Add,
                    contentDescription = "Create Post",
                    tint = AgroHubColors.DeepGreen
                )
            }
        }
    )
}

/**
 * Community Feed - Scrollable list of posts
 * Requirements: 6.1, 6.5
 */
@Composable
fun CommunityFeed(
    posts: List<FeedPost>,
    expandedPostId: Long?,
    onPostExpand: (Long) -> Unit,
    onLike: (Long) -> Unit,
    onComment: (Long) -> Unit,
    onLoadComments: (Long) -> Unit,
    postViewModel: PostViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = AgroHubSpacing.sm)
    ) {
        items(posts, key = { it.id }) { post ->
            FeedPostCard(
                post = post,
                isExpanded = expandedPostId == post.id,
                onExpand = {
                    onPostExpand(post.id)
                    if (expandedPostId != post.id) {
                        onLoadComments(post.id)
                    }
                },
                onLike = { onLike(post.id) },
                onComment = { onComment(post.id) },
                postViewModel = postViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AgroHubSpacing.md,
                        vertical = AgroHubSpacing.sm
                    )
            )
        }
    }
}

/**
 * Feed Post Card - Individual post with user info, content, images, and interactions
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */
@Composable
fun FeedPostCard(
    post: FeedPost,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onLike: () -> Unit,
    onComment: () -> Unit,
    postViewModel: PostViewModel,
    modifier: Modifier = Modifier
) {
    // Use mock comments for demo
    val mockComments = remember(post.id) { MockDataProvider.generateMockComments(post.id) }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(AgroHubSpacing.md),
        colors = CardDefaults.cardColors(
            containerColor = AgroHubColors.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.md)
        ) {
            // User Info Header
            PostHeader(
                username = post.author.username,
                avatarUrl = post.author.avatarUrl,
                timestamp = formatTimestamp(post.createdAt)
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Post Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = AgroHubColors.TextPrimary
            )
            
            // Post Image (if any)
            if (post.mediaUrl != null) {
                Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(AgroHubSpacing.sm)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Interaction Buttons
            PostInteractionButtons(
                likeCount = post.likeCount,
                commentCount = post.commentCount,
                isLiked = post.isLikedByCurrentUser,
                onLike = onLike,
                onComment = onComment
            )
            
            // Comments Section (if expanded)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                Divider(color = AgroHubColors.SurfaceLight)
                Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                
                CommentSection(
                    comments = mockComments,
                    postId = post.id,
                    postViewModel = postViewModel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // View Comments Button
            if (!isExpanded && post.commentCount > 0) {
                Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
                TextButton(
                    onClick = onExpand,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "View all ${post.commentCount} comments",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgroHubColors.DeepGreen
                    )
                }
            }
        }
    }
}

/**
 * Empty Feed Message
 */
@Composable
fun EmptyFeedMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = AgroHubIcons.People,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AgroHubColors.TextHint
            )
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            Text(
                text = "No posts yet",
                style = MaterialTheme.typography.titleLarge,
                color = AgroHubColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            Text(
                text = "Follow users to see their posts here",
                style = MaterialTheme.typography.bodyMedium,
                color = AgroHubColors.TextHint
            )
        }
    }
}

/**
 * Error Message with Retry
 */
@Composable
fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = AgroHubIcons.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgroHubColors.DeepGreen
                )
            ) {
                Text("Retry")
            }
        }
    }
}

/**
 * Format timestamp for display
 */
fun formatTimestamp(dateTime: java.time.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
    val duration = java.time.Duration.between(dateTime, now)
    
    return when {
        duration.toMinutes() < 1 -> "Just now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
        duration.toHours() < 24 -> "${duration.toHours()}h ago"
        duration.toDays() < 7 -> "${duration.toDays()}d ago"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}

/**
 * Post Header - User avatar, name, and timestamp
 * Requirements: 6.1
 */
@Composable
fun PostHeader(
    username: String,
    avatarUrl: String?,
    timestamp: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Avatar
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "User avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AgroHubColors.LightGreen),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AgroHubColors.LightGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgroHubColors.White
                )
            }
        }
        
        Spacer(modifier = Modifier.width(AgroHubSpacing.sm))
        
        // User Name and Timestamp
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = username,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AgroHubColors.TextPrimary
            )
            Text(
                text = timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = AgroHubColors.TextSecondary
            )
        }
        
        // More Options
        IconButton(onClick = { /* Handle more options */ }) {
            Icon(
                imageVector = AgroHubIcons.MoreVert,
                contentDescription = "More options",
                tint = AgroHubColors.TextSecondary
            )
        }
    }
}



/**
 * Post Interaction Buttons - Like and comment with icons and counts
 * Requirements: 6.3
 */
@Composable
fun PostInteractionButtons(
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Like Button
        InteractionButton(
            icon = if (isLiked) AgroHubIcons.Favorite else AgroHubIcons.Like,
            count = likeCount,
            label = "Like",
            isActive = isLiked,
            onClick = onLike,
            modifier = Modifier.weight(1f)
        )
        
        // Comment Button
        InteractionButton(
            icon = AgroHubIcons.Comment,
            count = commentCount,
            label = "Comment",
            isActive = false,
            onClick = onComment,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual Interaction Button
 * Requirements: 6.3
 */
@Composable
fun InteractionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isActive) AgroHubColors.DeepGreen else AgroHubColors.TextSecondary
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(AgroHubSpacing.xs))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * Comment Section - Display comments with nested threads
 * Requirements: 6.4
 */
@Composable
fun CommentSection(
    comments: List<Comment>,
    postId: Long,
    postViewModel: PostViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Comments",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AgroHubColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
        
        if (comments.isEmpty()) {
            Text(
                text = "No comments yet. Be the first to comment!",
                style = MaterialTheme.typography.bodyMedium,
                color = AgroHubColors.TextHint,
                modifier = Modifier.padding(vertical = AgroHubSpacing.md)
            )
        } else {
            comments.forEach { comment ->
                CommentItem(comment = comment)
                Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            }
        }
        
        // Add Comment Input
        AddCommentInput(
            postId = postId,
            onAddComment = { content ->
                postViewModel.addComment(postId, content)
            }
        )
    }
}

/**
 * Individual Comment Item
 * Requirements: 6.4
 */
@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // User Avatar
        if (comment.author.avatarUrl != null) {
            AsyncImage(
                model = comment.author.avatarUrl,
                contentDescription = "Commenter avatar",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AgroHubColors.LightGreen),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AgroHubColors.LightGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.author.username.take(1).uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgroHubColors.White
                )
            }
        }
        
        Spacer(modifier = Modifier.width(AgroHubSpacing.sm))
        
        // Comment Content
        Column(modifier = Modifier.weight(1f)) {
            // Comment Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.author.username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgroHubColors.TextPrimary
                )
                Text(
                    text = formatTimestamp(comment.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = AgroHubColors.TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
            
            // Comment Text
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = AgroHubColors.TextPrimary
            )
        }
    }
}

/**
 * Add Comment Input Field
 * Requirements: 6.4
 */
@Composable
fun AddCommentInput(
    postId: Long,
    onAddComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AgroHubSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = "Add a comment...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AgroHubColors.TextHint
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgroHubColors.DeepGreen,
                unfocusedBorderColor = AgroHubColors.SurfaceLight,
                focusedTextColor = AgroHubColors.TextPrimary,
                unfocusedTextColor = AgroHubColors.TextPrimary
            ),
            shape = RoundedCornerShape(AgroHubSpacing.lg),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.width(AgroHubSpacing.sm))
        
        IconButton(
            onClick = {
                if (commentText.isNotBlank()) {
                    onAddComment(commentText)
                    commentText = ""
                }
            },
            enabled = commentText.isNotBlank()
        ) {
            Icon(
                imageVector = AgroHubIcons.Send,
                contentDescription = "Send comment",
                tint = if (commentText.isNotBlank()) AgroHubColors.DeepGreen else AgroHubColors.TextHint
            )
        }
    }
}


