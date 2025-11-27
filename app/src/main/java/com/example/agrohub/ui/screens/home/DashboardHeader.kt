package com.example.agrohub.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * DashboardHeader - Header section for the dashboard
 * 
 * Displays:
 * - User greeting
 * - Notification icon with badge
 * 
 * Requirements: 2.1
 * 
 * @param userName Name of the user for greeting
 * @param notificationCount Number of unread notifications
 */
@Composable
fun DashboardHeader(
    userName: String,
    notificationCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AgroHubSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Greeting Text
        Text(
            text = "Hello, $userName! 👋",
            style = AgroHubTypography.Heading2,
            color = AgroHubColors.TextPrimary
        )
        
        // Notification Icon with Badge
        IconButton(
            onClick = { /* Navigate to notifications */ },
            modifier = Modifier.size(48.dp) // Minimum touch target
        ) {
            BadgedBox(
                badge = {
                    if (notificationCount > 0) {
                        Badge {
                            Text(
                                text = notificationCount.toString(),
                                style = AgroHubTypography.Caption
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = AgroHubIcons.Notification,
                    contentDescription = if (notificationCount > 0) {
                        "$notificationCount unread notifications"
                    } else {
                        "No new notifications"
                    },
                    tint = AgroHubColors.DeepGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
