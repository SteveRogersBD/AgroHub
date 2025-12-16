package com.example.agrohub.services

import com.example.agrohub.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * GeminiService - Service for interacting with Google Gemini AI
 *
 * Provides agriculture-focused AI assistance using Gemini API
 */
class GeminiService {

    companion object {
        // API key loaded from local.properties via BuildConfig
        private val API_KEY = BuildConfig.GEMINI_API_KEY

        // System prompt to make Gemini focus on agriculture
        private val SYSTEM_PROMPT = """
            You are an expert agricultural assistant helping farmers with their questions.
            Your expertise includes:
            - Crop management and cultivation
            - Pest and disease identification and treatment
            - Soil health and fertilization
            - Irrigation and water management
            - Weather-related farming advice
            - Sustainable farming practices
            - Farm equipment and technology
            
            Provide clear, practical, and actionable advice.
            Keep responses concise but informative.
            If a question is not related to agriculture, politely redirect to farming topics.
        """.trimIndent()
    }

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = API_KEY
    )

    /**
     * Send a message to Gemini and get a response
     *
     * @param userMessage The user's question or message
     * @return AI-generated response focused on agriculture
     */
    suspend fun sendMessage(userMessage: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = content {
                    text(SYSTEM_PROMPT)
                    text("\n\nUser question: $userMessage")
                }

                val response = model.generateContent(prompt)
                response.text ?: "I'm sorry, I couldn't generate a response. Please try again."
            } catch (e: Exception) {
                "Error: ${e.message ?: "Unable to connect to AI service. Please check your internet connection."}"
            }
        }
    }

    /**
     * Send a message with conversation history for context-aware responses
     *
     * @param conversationHistory List of previous messages (user and bot)
     * @param newMessage The new user message
     * @return AI-generated response
     */
    suspend fun sendMessageWithHistory(
        conversationHistory: List<Pair<String, String>>, // Pair<role, message>
        newMessage: String
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildString {
                    append(SYSTEM_PROMPT)
                    append("\n\nConversation history:\n")
                    conversationHistory.forEach { (role, message) ->
                        append("$role: $message\n")
                    }
                    append("\nUser: $newMessage")
                }
                
                val response = model.generateContent(prompt)
                response.text ?: "I'm sorry, I couldn't generate a response. Please try again."
            } catch (e: Exception) {
                "Error: ${e.message ?: "Unable to connect to AI service. Please check your internet connection."}"
            }
        }
    }
}
