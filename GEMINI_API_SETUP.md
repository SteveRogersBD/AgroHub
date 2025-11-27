# Gemini AI Chatbot Setup Guide

## ✅ What's Been Implemented

Your AgroHub chatbot is now powered by Google Gemini AI! The bot will:
- Answer agriculture-related questions
- Provide farming advice
- Help with crop management, pest control, soil health, etc.
- Show a loading indicator while thinking
- Maintain conversation context

## 🔑 Get Your Gemini API Key

1. Go to [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy the generated API key

## 📝 Add Your API Key

Open this file:
```
app/src/main/java/com/example/agrohub/services/GeminiService.kt
```

Replace this line:
```kotlin
private const val API_KEY = "YOUR_GEMINI_API_KEY_HERE"
```

With your actual key:
```kotlin
private const val API_KEY = "AIza..."  // Your actual Gemini API key
```

## 🚀 How It Works

1. **User sends a message** → Displayed in green bubble on the right
2. **Loading indicator appears** → Shows the bot is thinking
3. **Gemini processes the question** → With agriculture-focused context
4. **Bot responds** → Displayed in white bubble on the left with a leaf icon

## 🌾 Agriculture Focus

The bot has a system prompt that makes it an expert in:
- Crop management and cultivation
- Pest and disease identification
- Soil health and fertilization
- Irrigation and water management
- Weather-related farming advice
- Sustainable farming practices
- Farm equipment and technology

## 📱 Testing the Chatbot

1. Run the app
2. Tap the green floating chat button (on Home or Farm screen)
3. Ask questions like:
   - "How do I prevent tomato blight?"
   - "What's the best time to plant wheat?"
   - "How can I improve soil fertility?"
   - "What are signs of nitrogen deficiency in crops?"

## 🔒 Security Note

⚠️ **Important**: The API key is currently hardcoded. For production:
- Store it in `local.properties`
- Use BuildConfig to inject at build time
- Never commit API keys to version control

Example for production:
```kotlin
// In local.properties
gemini.api.key=YOUR_KEY_HERE

// In build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("gemini.api.key")}\"")
    }
}

// In GeminiService.kt
private const val API_KEY = BuildConfig.GEMINI_API_KEY
```

## 📦 Dependencies Added

- `com.google.ai.client.generativeai:generativeai:0.9.0`

## 🎯 Features

✅ Real-time AI responses
✅ Agriculture-focused expertise
✅ Loading indicators
✅ Error handling
✅ Clean chat UI
✅ Conversation history support (ready for future enhancement)

## 🐛 Troubleshooting

**"Error: Unable to connect"**
- Check your internet connection
- Verify API key is correct
- Ensure Gemini API is enabled in Google Cloud Console

**Slow responses**
- Normal for first request (model initialization)
- Subsequent requests are faster

**API quota exceeded**
- Gemini has free tier limits
- Check your usage at [Google AI Studio](https://aistudio.google.com/)
