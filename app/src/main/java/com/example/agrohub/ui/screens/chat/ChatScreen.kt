package com.example.agrohub.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agrohub.models.ChatMessage
import com.example.agrohub.services.GeminiService
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTypography
import kotlinx.coroutines.launch

/**
 * Chat Screen (Agri-Bot)
 * 
 * Displays a WhatsApp/ChatGPT-style chat interface for the Agri-Bot assistant.
 * Features:
 * - Message list with LazyColumn
 * - User messages (right-aligned, green background)
 * - Bot messages (left-aligned, white background)
 * - Timestamps on all messages
 * - Input field with send button
 * - Fade-in and slide-up animations for new messages
 * 
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    // Start with empty messages - no placeholders
    var messages by remember { 
        mutableStateOf(listOf(
            ChatMessage(
                id = "welcome_msg",
                text = "Hello! I'm your Agri-Bot assistant. I can help you with farming questions, crop management, pest control, weather advice, and more. How can I assist you today?",
                isUser = false,
                timestamp = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                    .format(java.util.Date()),
                icon = AgroHubIcons.Leaf
            )
        ))
    }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val geminiService = remember { GeminiService() }
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-scroll to bottom when new messages are added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Agri-Bot Assistant",
                            style = AgroHubTypography.Heading3,
                            color = AgroHubColors.White
                        )
                        Text(
                            text = if (isLoading) "Typing..." else "Online",
                            style = AgroHubTypography.Caption,
                            color = AgroHubColors.LightGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgroHubColors.DeepGreen
                )
            )
        },
        bottomBar = {
            ChatInputField(
                value = inputText,
                onValueChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank() && !isLoading) {
                        val userMessageText = inputText
                        inputText = ""
                        
                        // Add user message
                        val userMessage = ChatMessage(
                            id = "msg_${System.currentTimeMillis()}",
                            text = userMessageText,
                            isUser = true,
                            timestamp = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                .format(java.util.Date()),
                            icon = null
                        )
                        messages = messages + userMessage
                        isLoading = true
                        
                        // Get AI response from Gemini
                        coroutineScope.launch {
                            val aiResponse = geminiService.sendMessage(userMessageText)
                            
                            // Add bot response
                            val botMessage = ChatMessage(
                                id = "msg_${System.currentTimeMillis()}",
                                text = aiResponse,
                                isUser = false,
                                timestamp = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                    .format(java.util.Date()),
                                icon = AgroHubIcons.Leaf
                            )
                            messages = messages + botMessage
                            isLoading = false
                        }
                    }
                },
                isLoading = isLoading
            )
        }
    ) { paddingValues ->
        ChatMessageList(
            messages = messages,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

/**
 * Chat Message List
 * 
 * Displays a scrollable list of chat messages with animations.
 * Uses LazyColumn for efficient rendering of large message lists.
 * 
 * Requirements: 5.1, 5.5
 */
@Composable
fun ChatMessageList(
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = modifier
            .background(AgroHubColors.BackgroundLight)
            .padding(horizontal = AgroHubSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
    ) {
        item {
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
        }
        
        items(
            items = messages,
            key = { it.id }
        ) { message ->
            // Animate each message with fade-in and slide-up
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300)) +
                        slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(300)
                        )
            ) {
                ChatBubble(message = message)
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
        }
    }
}

/**
 * Chat Bubble
 * 
 * Displays a single chat message with appropriate styling based on sender.
 * - User messages: right-aligned, green background
 * - Bot messages: left-aligned, white background
 * - All messages include timestamps
 * 
 * Requirements: 5.2, 5.3
 */
@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (message.isUser) 16.dp else 4.dp,
                        topEnd = if (message.isUser) 4.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(
                    if (message.isUser) AgroHubColors.DeepGreen
                    else AgroHubColors.White
                )
                .padding(AgroHubSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm),
            verticalAlignment = Alignment.Top
        ) {
            // Show icon for bot messages
            if (!message.isUser && message.icon != null) {
                Icon(
                    imageVector = message.icon,
                    contentDescription = null,
                    tint = AgroHubColors.DeepGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = message.text,
                    style = AgroHubTypography.Body,
                    color = if (message.isUser) AgroHubColors.White
                    else AgroHubColors.TextPrimary
                )
            }
        }
        
        // Timestamp
        Text(
            text = message.timestamp,
            style = AgroHubTypography.Caption,
            color = AgroHubColors.TextSecondary,
            modifier = Modifier.padding(
                horizontal = AgroHubSpacing.sm,
                vertical = AgroHubSpacing.xs
            )
        )
    }
}

/**
 * Chat Input Field
 * 
 * Text input field with send button for composing messages.
 * 
 * Requirements: 5.4
 */
@Composable
fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AgroHubColors.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Ask me anything about farming...",
                        style = AgroHubTypography.Body,
                        color = AgroHubColors.TextHint
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AgroHubColors.BackgroundLight,
                    unfocusedContainerColor = AgroHubColors.BackgroundLight,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = AgroHubColors.TextPrimary,
                    unfocusedTextColor = AgroHubColors.TextPrimary
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (value.isNotBlank() && !isLoading) AgroHubColors.DeepGreen
                        else AgroHubColors.SurfaceLight
                    )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AgroHubColors.DeepGreen,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        tint = if (value.isNotBlank()) AgroHubColors.White
                        else AgroHubColors.TextHint
                    )
                }
            }
        }
    }
}
