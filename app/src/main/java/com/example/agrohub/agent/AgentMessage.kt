package com.example.agrohub.agent

import java.util.UUID

/**
 * Represents a message in the agent conversation
 */
data class AgentMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageRole {
    USER,
    AGENT,
    SYSTEM
}
