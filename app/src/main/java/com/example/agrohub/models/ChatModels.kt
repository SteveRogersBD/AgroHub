package com.example.agrohub.models

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data model representing a chat message in the Agri-Bot interface
 */
data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String,
    val icon: ImageVector?
)
