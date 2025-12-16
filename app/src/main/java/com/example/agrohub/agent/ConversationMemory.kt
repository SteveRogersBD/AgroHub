package com.example.agrohub.agent

/**
 * Manages conversation history and context for the AI agent
 */
class ConversationMemory(
    private val maxHistorySize: Int = 20
) {
    private val messages = mutableListOf<AgentMessage>()
    
    fun addMessage(message: AgentMessage) {
        messages.add(message)
        // Keep only recent messages to avoid token limits
        if (messages.size > maxHistorySize) {
            messages.removeAt(0)
        }
    }
    
    fun getMessages(): List<AgentMessage> = messages.toList()
    
    fun getRecentMessages(count: Int): List<AgentMessage> {
        return messages.takeLast(count)
    }
    
    fun clear() {
        messages.clear()
    }
    
    fun getConversationContext(): String {
        return messages.joinToString("\n") { message ->
            when (message.role) {
                MessageRole.USER -> "User: ${message.content}"
                MessageRole.AGENT -> "Agent: ${message.content}"
                MessageRole.SYSTEM -> "System: ${message.content}"
            }
        }
    }
}
