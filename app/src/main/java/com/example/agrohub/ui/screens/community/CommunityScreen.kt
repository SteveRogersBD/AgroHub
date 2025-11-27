package com.example.agrohub.ui.screens.community

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.agrohub.data.MockDataProvider
import com.example.agrohub.models.Comment
import com.example.agrohub.models.Post
import com.example.agrohub.ui.components.buttons.AgroHubFAB
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing

/**
 * Community Screen - Social feed for farmer interactions
 * 
 * Features:
 * - Scrollable feed with posts
 * - Post cards with user info, content, images
 * - Like, comment, share interactions
 * - Comment sections with nested threads
 * - Create post FAB
 * - Staggered animations
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6
 */
@Composable
fun CommunityScreen(navController: NavController) {
    val posts = remember { MockDataProvider.generateCommunityPosts() }
    var expandedPostId by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroHubColors.BackgroundLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            CommunityHeader()
            
            // Community Feed
            CommunityFeed(
                posts = posts,
                expandedPostId = expandedPostId,
                onPostExpand = { postId ->
                    expandedPostId = if (expandedPostId == postId) null else postId
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Create Post FAB
        CreatePostFAB(
            onClick = { /* Handle create post */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AgroHubSpacing.md)
        )
    }
}

/**
 * Community Header
 * Requirements: 6.1
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHeader() {
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
            IconButton(onClick = { /* Handle search */ }) {
                Icon(
                    imageVector = AgroHubIcons.Search,
                    contentDescription = "Search",
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
    posts: List<Post>,
    expandedPostId: String?,
    onPostExpand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = AgroHubSpacing.sm)
    ) {
        itemsIndexed(posts) { index, post ->
            // Staggered animation for feed items
            val animationDelay = (index * 50).coerceAtMost(300)
            
            AnimatedPostCard(
                post = post,
                isExpanded = expandedPostId == post.id,
                onExpand = { onPostExpand(post.id) },
                animationDelay = animationDelay,
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
 * Animated Post Card wrapper with staggered animation
 * Requirements: 6.5
 */
@Composable
fun AnimatedPostCard(
    post: Post,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            initialOffsetY = { it / 4 }
        )
    ) {
        PostCard(
            post = post,
            isExpanded = isExpanded,
            onExpand = onExpand,
            onLike = { /* Handle like */ },
            onComment = { /* Handle comment */ },
            onShare = { /* Handle share */ },
            modifier = modifier
        )
    }
}

/**
 * Post Card - Individual post with user info, content, images, and interactions
 * Requirements: 6.1, 6.2, 6.3
 */
@Composable
fun PostCard(
    post: Post,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                userName = post.userName,
                userAvatar = post.userAvatar,
                timestamp = post.timestamp
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Post Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = AgroHubColors.TextPrimary
            )
            
            // Post Images (if any)
            if (post.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                PostImages(images = post.images)
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Interaction Buttons
            PostInteractionButtons(
                post = post,
                onLike = onLike,
                onComment = onComment,
                onShare = onShare
            )
            
            // Comments Section (if expanded)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                Divider(color = AgroHubColors.SurfaceLight)
                Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
                CommentSection(
                    comments = MockDataProvider.generateComments(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // View Comments Button
            if (!isExpanded && post.comments > 0) {
                Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
                TextButton(
                    onClick = onExpand,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "View all ${post.comments} comments",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgroHubColors.DeepGreen
                    )
                }
            }
        }
    }
}

/**
 * Post Header - User avatar, name, and timestamp
 * Requirements: 6.1
 */
@Composable
fun PostHeader(
    userName: String,
    userAvatar: Int,
    timestamp: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Avatar
        Image(
            painter = painterResource(id = userAvatar),
            contentDescription = "User avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AgroHubColors.LightGreen),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(AgroHubSpacing.sm))
        
        // User Name and Timestamp
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userName,
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
                imageVector = AgroHubIcons.Settings,
                contentDescription = "More options",
                tint = AgroHubColors.TextSecondary
            )
        }
    }
}

/**
 * Post Images - Display images with proper aspect ratios and rounded corners
 * Requirements: 6.2
 */
@Composable
fun PostImages(images: List<Int>) {
    when (images.size) {
        1 -> {
            // Single image - full width
            Image(
                painter = painterResource(id = images[0]),
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(AgroHubSpacing.sm)),
                contentScale = ContentScale.Crop
            )
        }
        2 -> {
            // Two images - side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.xs)
            ) {
                images.forEach { imageRes ->
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "Post image",
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .clip(RoundedCornerShape(AgroHubSpacing.sm)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        else -> {
            // Three or more images - grid layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.xs)
            ) {
                // First image full width
                Image(
                    painter = painterResource(id = images[0]),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(AgroHubSpacing.sm)),
                    contentScale = ContentScale.Crop
                )
                
                // Remaining images in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.xs)
                ) {
                    images.drop(1).take(2).forEach { imageRes ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                        ) {
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = "Post image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(AgroHubSpacing.sm)),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Show "+N more" overlay if there are more images
                            if (images.size > 3 && imageRes == images[2]) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = AgroHubColors.CharcoalText.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(AgroHubSpacing.sm)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${images.size - 3}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AgroHubColors.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Post Interaction Buttons - Like, comment, share with icons and counts
 * Requirements: 6.3
 */
@Composable
fun PostInteractionButtons(
    post: Post,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Like Button
        InteractionButton(
            icon = AgroHubIcons.Like,
            count = post.likes,
            label = "Like",
            isActive = post.isLiked,
            onClick = onLike,
            modifier = Modifier.weight(1f)
        )
        
        // Comment Button
        InteractionButton(
            icon = AgroHubIcons.Comment,
            count = post.comments,
            label = "Comment",
            isActive = false,
            onClick = onComment,
            modifier = Modifier.weight(1f)
        )
        
        // Share Button
        InteractionButton(
            icon = AgroHubIcons.Share,
            count = post.shares,
            label = "Share",
            isActive = false,
            onClick = onShare,
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
        
        comments.forEach { comment ->
            CommentItem(comment = comment)
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
        }
        
        // Add Comment Input
        AddCommentInput()
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
        Image(
            painter = painterResource(id = comment.userAvatar),
            contentDescription = "Commenter avatar",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(AgroHubColors.LightGreen),
            contentScale = ContentScale.Crop
        )
        
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
                    text = comment.userName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgroHubColors.TextPrimary
                )
                Text(
                    text = comment.timestamp,
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
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
            
            // Comment Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
            ) {
                TextButton(
                    onClick = { /* Handle like comment */ },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = AgroHubIcons.Like,
                        contentDescription = "Like comment",
                        modifier = Modifier.size(16.dp),
                        tint = AgroHubColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(AgroHubSpacing.xs))
                    Text(
                        text = comment.likes.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = AgroHubColors.TextSecondary
                    )
                }
                
                TextButton(
                    onClick = { /* Handle reply */ },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Reply",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgroHubColors.DeepGreen
                    )
                }
            }
        }
    }
}

/**
 * Add Comment Input Field
 * Requirements: 6.4
 */
@Composable
fun AddCommentInput() {
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
                    // Handle send comment
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

/**
 * Create Post FAB - Floating action button for creating new posts
 * Requirements: 6.6
 */
@Composable
fun CreatePostFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AgroHubFAB(
        icon = AgroHubIcons.Add,
        onClick = onClick,
        modifier = modifier
    )
}
