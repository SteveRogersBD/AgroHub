package com.example.agrohub.ui.screens.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.agrohub.data.MockDataProvider
import com.example.agrohub.models.Activity
import com.example.agrohub.models.Note
import com.example.agrohub.models.SettingItem
import com.example.agrohub.ui.components.cards.StatCard
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * ProfileScreen - User profile and settings screen
 * 
 * Displays:
 * - Profile header with avatar, name, contact info, edit button
 * - User statistics (farms, posts, marketplace items)
 * - Settings list with icons and navigation
 * - Saved notes section
 * - Activity history section
 * 
 * Features:
 * - Scrollable layout
 * - Fade-in animations
 * - Interactive elements
 * 
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5
 * 
 * @param navController Navigation controller for screen navigation
 */
@Composable
fun ProfileScreen(navController: NavController) {
    // Load mock data
    val notes = remember { MockDataProvider.generateProfileNotes() }
    val activities = remember { MockDataProvider.generateActivityHistory() }
    val settings = remember { MockDataProvider.generateSettingsItems() }
    
    // Animation state for fade-in
    var visible by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "screen_alpha"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(AgroHubColors.LightGreen),
        contentPadding = PaddingValues(AgroHubSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.lg)
    ) {
        // Profile Header
        item {
            ProfileHeader(
                userName = "Rajesh Kumar",
                userAvatar = android.R.drawable.ic_menu_gallery,
                email = "rajesh.kumar@example.com",
                phone = "+91 98765 43210",
                onEdit = { /* Handle edit action */ }
            )
        }
        
        // User Stats Section
        item {
            UserStatsSection(
                farmsCount = 5,
                postsCount = 12,
                marketItemsCount = 3
            )
        }
        
        // Settings List
        item {
            SettingsList(settings = settings)
        }
        
        // Saved Notes Section
        item {
            SavedNotesSection(notes = notes)
        }
        
        // Activity History Section
        item {
            ActivityHistorySection(activities = activities)
        }
    }
}

/**
 * ProfileHeader - Displays user profile information
 * 
 * Requirements: 9.1
 * 
 * @param userName User's display name
 * @param userAvatar Resource ID for user avatar
 * @param email User's email address
 * @param phone User's phone number
 * @param onEdit Callback for edit button click
 */
@Composable
fun ProfileHeader(
    userName: String,
    userAvatar: Int,
    email: String,
    phone: String,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AgroHubColors.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Image(
                painter = painterResource(id = userAvatar),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AgroHubColors.LightGreen)
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // User Name
            Text(
                text = userName,
                style = AgroHubTypography.Heading2,
                color = AgroHubColors.CharcoalText
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
            
            // Email
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.xs)
            ) {
                Icon(
                    imageVector = AgroHubIcons.Email,
                    contentDescription = "Email",
                    tint = AgroHubColors.DeepGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = email,
                    style = AgroHubTypography.Body,
                    color = AgroHubColors.CharcoalText.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
            
            // Phone
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.xs)
            ) {
                Icon(
                    imageVector = AgroHubIcons.Phone,
                    contentDescription = "Phone",
                    tint = AgroHubColors.DeepGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = phone,
                    style = AgroHubTypography.Body,
                    color = AgroHubColors.CharcoalText.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // Edit Button
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgroHubColors.DeepGreen
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = AgroHubIcons.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(AgroHubSpacing.xs))
                Text(
                    text = "Edit Profile",
                    style = AgroHubTypography.Body.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * UserStatsSection - Displays user statistics
 * 
 * Requirements: 9.2
 * 
 * @param farmsCount Number of farms
 * @param postsCount Number of community posts
 * @param marketItemsCount Number of marketplace items
 */
@Composable
fun UserStatsSection(
    farmsCount: Int,
    postsCount: Int,
    marketItemsCount: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
    ) {
        Text(
            text = "My Statistics",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.CharcoalText
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
        ) {
            // Farms Count
            StatCard(
                title = "Farms",
                value = farmsCount.toString(),
                icon = AgroHubIcons.Field,
                gradient = Brush.linearGradient(
                    colors = listOf(
                        AgroHubColors.DeepGreen,
                        AgroHubColors.MediumGreen
                    )
                ),
                modifier = Modifier.weight(1f)
            )
            
            // Posts Count
            StatCard(
                title = "Posts",
                value = postsCount.toString(),
                icon = AgroHubIcons.Community,
                gradient = Brush.linearGradient(
                    colors = listOf(
                        AgroHubColors.SkyBlue,
                        AgroHubColors.SkyBlue.copy(alpha = 0.7f)
                    )
                ),
                modifier = Modifier.weight(1f)
            )
            
            // Market Items Count
            StatCard(
                title = "Listed",
                value = marketItemsCount.toString(),
                icon = AgroHubIcons.Cart,
                gradient = Brush.linearGradient(
                    colors = listOf(
                        AgroHubColors.GoldenHarvest,
                        AgroHubColors.GoldenHarvest.copy(alpha = 0.7f)
                    )
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * SettingsList - Displays settings menu items
 * 
 * Requirements: 9.3
 * 
 * @param settings List of setting items
 */
@Composable
fun SettingsList(settings: List<SettingItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
    ) {
        Text(
            text = "Settings",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.CharcoalText
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = AgroHubColors.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                settings.forEachIndexed { index, setting ->
                    SettingsItem(
                        title = setting.title,
                        icon = setting.icon,
                        onClick = { /* Handle navigation to setting.route */ }
                    )
                    
                    // Add divider between items (except last)
                    if (index < settings.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = AgroHubSpacing.md),
                            color = AgroHubColors.LightGreen
                        )
                    }
                }
            }
        }
    }
}

/**
 * SettingsItem - Individual settings menu item
 * 
 * Requirements: 9.3
 * 
 * @param title Setting title
 * @param icon Setting icon
 * @param onClick Click callback
 */
@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(AgroHubSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AgroHubColors.DeepGreen,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = AgroHubTypography.Body,
                color = AgroHubColors.CharcoalText
            )
        }
        
        Icon(
            imageVector = AgroHubIcons.Forward,
            contentDescription = "Navigate",
            tint = AgroHubColors.CharcoalText.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * SavedNotesSection - Displays saved notes
 * 
 * Requirements: 9.4
 * 
 * @param notes List of saved notes
 */
@Composable
fun SavedNotesSection(notes: List<Note>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
    ) {
        Text(
            text = "Saved Notes",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.CharcoalText
        )
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
        ) {
            notes.take(5).forEach { note ->
                NoteCard(note = note)
            }
        }
    }
}

/**
 * NoteCard - Individual note card
 * 
 * Requirements: 9.4
 * 
 * @param note Note data
 */
@Composable
fun NoteCard(note: Note) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AgroHubColors.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = note.title,
                        style = AgroHubTypography.Body.copy(fontWeight = FontWeight.Bold),
                        color = AgroHubColors.CharcoalText
                    )
                    
                    Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
                    
                    Text(
                        text = note.preview,
                        style = AgroHubTypography.Caption,
                        color = AgroHubColors.CharcoalText.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
                
                Icon(
                    imageVector = AgroHubIcons.Note,
                    contentDescription = "Note",
                    tint = AgroHubColors.GoldenHarvest,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            Text(
                text = note.timestamp,
                style = AgroHubTypography.Caption,
                color = AgroHubColors.CharcoalText.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * ActivityHistorySection - Displays activity history
 * 
 * Requirements: 9.5
 * 
 * @param activities List of activities
 */
@Composable
fun ActivityHistorySection(activities: List<Activity>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
    ) {
        Text(
            text = "Recent Activity",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.CharcoalText
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = AgroHubColors.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                activities.take(8).forEachIndexed { index, activity ->
                    ActivityCard(activity = activity)
                    
                    // Add divider between items (except last)
                    if (index < activities.size - 1 && index < 7) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = AgroHubSpacing.md),
                            color = AgroHubColors.LightGreen
                        )
                    }
                }
            }
        }
    }
}

/**
 * ActivityCard - Individual activity item
 * 
 * Requirements: 9.5
 * 
 * @param activity Activity data
 */
@Composable
fun ActivityCard(activity: Activity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AgroHubSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Activity Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AgroHubColors.LightGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = activity.icon,
                contentDescription = activity.description,
                tint = AgroHubColors.DeepGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Activity Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = activity.description,
                style = AgroHubTypography.Body,
                color = AgroHubColors.CharcoalText
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
            
            Text(
                text = activity.timestamp,
                style = AgroHubTypography.Caption,
                color = AgroHubColors.CharcoalText.copy(alpha = 0.5f)
            )
        }
    }
}
