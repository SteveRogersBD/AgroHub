package com.example.agrohub.models

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data model representing a saved note
 */
data class Note(
    val id: String,
    val title: String,
    val preview: String,
    val timestamp: String
)

/**
 * Data model representing an activity in the user's history
 */
data class Activity(
    val id: String,
    val description: String,
    val timestamp: String,
    val icon: ImageVector
)

/**
 * Data model representing a settings menu item
 */
data class SettingItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)
