package com.example.agrohub.ui.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.agrohub.ui.components.buttons.PrimaryButton
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * Empty state component with illustrations and messages.
 * Provides contextual feedback when content is not available.
 * 
 * Requirements: 12.4
 */

/**
 * Generic empty state component
 * 
 * @param icon Icon to display (agricultural themed)
 * @param title Main title text
 * @param message Descriptive message text
 * @param modifier Modifier for styling
 * @param actionText Optional action button text
 * @param onActionClick Optional action button click handler
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon illustration
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(120.dp),
                tint = AgroHubColors.LightGreen
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
            
            // Title
            Text(
                text = title,
                style = AgroHubTypography.Heading2,
                color = AgroHubColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Message
            Text(
                text = message,
                style = AgroHubTypography.Body,
                color = AgroHubColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            
            // Optional action button
            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
                
                PrimaryButton(
                    text = actionText,
                    onClick = onActionClick
                )
            }
        }
    }
}

/**
 * Empty farms state - shown when user has no farms
 */
@Composable
fun EmptyFarmsState(
    modifier: Modifier = Modifier,
    onAddFarm: () -> Unit
) {
    EmptyState(
        icon = AgroHubIcons.Field,
        title = "No Farms Yet",
        message = "Start your farming journey by adding your first farm. Track crops, monitor health, and manage your agricultural operations.",
        modifier = modifier,
        actionText = "Add Your First Farm",
        onActionClick = onAddFarm
    )
}

/**
 * Empty crops state - shown when farm has no crops
 */
@Composable
fun EmptyCropsState(
    modifier: Modifier = Modifier,
    onAddCrop: (() -> Unit)? = null
) {
    EmptyState(
        icon = AgroHubIcons.Plant,
        title = "No Crops Planted",
        message = "Add crops to your farm to start tracking their health and growth progress.",
        modifier = modifier,
        actionText = if (onAddCrop != null) "Add Crop" else null,
        onActionClick = onAddCrop
    )
}

/**
 * Empty community posts state - shown when feed has no posts
 */
@Composable
fun EmptyPostsState(
    modifier: Modifier = Modifier,
    onCreatePost: (() -> Unit)? = null
) {
    EmptyState(
        icon = AgroHubIcons.Community,
        title = "No Posts Yet",
        message = "Be the first to share your farming experiences, tips, and questions with the community.",
        modifier = modifier,
        actionText = if (onCreatePost != null) "Create Post" else null,
        onActionClick = onCreatePost
    )
}

/**
 * Empty marketplace products state - shown when no products available
 */
@Composable
fun EmptyProductsState(
    modifier: Modifier = Modifier,
    onAddProduct: (() -> Unit)? = null
) {
    EmptyState(
        icon = AgroHubIcons.Cart,
        title = "No Products Available",
        message = "Check back later for agricultural products, tools, and supplies from local farmers and suppliers.",
        modifier = modifier,
        actionText = if (onAddProduct != null) "List a Product" else null,
        onActionClick = onAddProduct
    )
}

/**
 * Empty chat messages state - shown when chat has no messages
 */
@Composable
fun EmptyChatState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = AgroHubIcons.Chat,
        title = "Start a Conversation",
        message = "Ask me anything about farming, crop care, pest management, or agricultural best practices. I'm here to help!",
        modifier = modifier
    )
}

/**
 * Empty weather alerts state - shown when no weather alerts
 */
@Composable
fun EmptyWeatherAlertsState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = AgroHubIcons.Sun,
        title = "No Weather Alerts",
        message = "Great news! There are no weather warnings or alerts for your area. Conditions are favorable for farming activities.",
        modifier = modifier
    )
}

/**
 * Empty notes state - shown when user has no saved notes
 */
@Composable
fun EmptyNotesState(
    modifier: Modifier = Modifier,
    onCreateNote: (() -> Unit)? = null
) {
    EmptyState(
        icon = AgroHubIcons.Note,
        title = "No Saved Notes",
        message = "Keep track of important farming observations, reminders, and insights by creating notes.",
        modifier = modifier,
        actionText = if (onCreateNote != null) "Create Note" else null,
        onActionClick = onCreateNote
    )
}

/**
 * Empty activity history state - shown when no activity recorded
 */
@Composable
fun EmptyActivityState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = AgroHubIcons.History,
        title = "No Activity Yet",
        message = "Your farming activities and actions will appear here as you use the app.",
        modifier = modifier
    )
}

/**
 * Empty search results state - shown when search returns no results
 */
@Composable
fun EmptySearchResultsState(
    modifier: Modifier = Modifier,
    searchQuery: String = ""
) {
    val message = if (searchQuery.isNotEmpty()) {
        "No results found for \"$searchQuery\". Try different keywords or check your spelling."
    } else {
        "No results found. Try adjusting your search criteria."
    }
    
    EmptyState(
        icon = AgroHubIcons.Search,
        title = "No Results Found",
        message = message,
        modifier = modifier
    )
}

/**
 * Empty tasks state - shown when no pending tasks
 */
@Composable
fun EmptyTasksState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = AgroHubIcons.Check,
        title = "All Caught Up!",
        message = "You have no pending tasks. Great job staying on top of your farm management!",
        modifier = modifier
    )
}

/**
 * No internet connection state - shown when offline
 */
@Composable
fun NoConnectionState(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    EmptyState(
        icon = AgroHubIcons.Disease,
        title = "No Connection",
        message = "Unable to connect to the internet. Please check your connection and try again.",
        modifier = modifier,
        actionText = if (onRetry != null) "Retry" else null,
        onActionClick = onRetry
    )
}

/**
 * Generic error state - shown when an error occurs
 */
@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    title: String = "Something Went Wrong",
    message: String = "An unexpected error occurred. Please try again later.",
    onRetry: (() -> Unit)? = null
) {
    EmptyState(
        icon = AgroHubIcons.CriticalStatus.icon,
        title = title,
        message = message,
        modifier = modifier,
        actionText = if (onRetry != null) "Try Again" else null,
        onActionClick = onRetry
    )
}
