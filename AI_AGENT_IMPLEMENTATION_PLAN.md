# AI Agent Implementation Plan for AgroHub

## Overview
This document outlines the plan for building an AI agent system inside the AgroHub Android app using Kotlin with Gemini as the brain.

## Is It Possible?
**Yes, absolutely!** The foundation is already in place with the existing GeminiService.

## Architecture

### What We Already Have ✓
- Gemini API integration via GeminiService
- Kotlin coroutines for async operations
- Jetpack Compose UI framework

### Agent Architecture
```
AgentService (orchestrator)
├── GeminiService (brain/reasoning)
├── ConversationManager (memory)
├── ToolRegistry (available actions)
│   ├── CropDatabaseTool
│   ├── WeatherTool
│   ├── NoteTool
│   └── SearchTool
└── ActionExecutor (performs actions)
```

### How It Works
1. User sends message → Agent analyzes intent using Gemini
2. Gemini decides which tool(s) to use (via structured prompts)
3. Agent executes tools and gathers results
4. Agent sends results back to Gemini for final response
5. User gets natural language answer + actions performed

### Example Flow
```
User: "What should I plant in December?"
↓
Agent → Gemini: Analyze intent
↓
Gemini: "Need location + crop database"
↓
Agent: Execute GetLocationTool + QueryCropDatabaseTool
↓
Agent → Gemini: "Location: Punjab, Available crops: wheat, mustard..."
↓
Gemini: Generates natural response with recommendations
↓
User: "Based on your location and season, I recommend wheat..."
```

## SDK/Libraries Required

### Already in Project ✓
- **`com.google.ai.client.generativeai`** (v0.9.0) - Existing Gemini SDK, supports function calling natively
- **Kotlin Coroutines** - For async agent operations
- **Jetpack Compose** - For the UI

### What to Add

**For Agent Framework:**
- **Nothing extra needed!** - Build custom agent logic using existing Gemini SDK
- The Gemini SDK already supports:
  - Function/tool declarations
  - Multi-turn conversations with history
  - Structured outputs

**Optional (for enhanced features):**
- **Room Database** - For persistent conversation memory
- **Kotlinx Serialization** - For parsing tool responses (lightweight, ~100KB)
  ```gradle
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
  ```

### Why No Special Agent Library?
The Gemini SDK has built-in function calling, so we don't need LangChain or other agent frameworks. The "agent" is just orchestration code written in Kotlin.

## Code Example: Tool Definition

```kotlin
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionType
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.defineFunction

// 1. Define your tool as a Kotlin function
suspend fun getCropInfo(cropName: String, season: String): String {
    // Your actual implementation - could query database, API, etc.
    return when (cropName.lowercase()) {
        "wheat" -> "Wheat grows best in $season. Requires 20-25°C temperature..."
        "rice" -> "Rice needs flooded fields. Best in monsoon season..."
        else -> "Crop information not found for $cropName"
    }
}

suspend fun getWeatherForecast(location: String): String {
    // Call weather API
    return "Weather in $location: 28°C, Sunny, Good for farming"
}

// 2. Register tools with Gemini
val model = GenerativeModel(
    modelName = "gemini-2.0-flash",
    apiKey = API_KEY,
    tools = listOf(
        Tool(
            functionDeclarations = listOf(
                // Define the getCropInfo tool
                defineFunction(
                    name = "getCropInfo",
                    description = "Get detailed information about a specific crop including growing conditions, season, and care tips",
                    parameters = listOf(
                        Schema.str("cropName", "Name of the crop (e.g., wheat, rice, corn)"),
                        Schema.str("season", "Current season (e.g., winter, summer, monsoon)")
                    )
                ),
                // Define the getWeatherForecast tool
                defineFunction(
                    name = "getWeatherForecast",
                    description = "Get current weather forecast for a location",
                    parameters = listOf(
                        Schema.str("location", "City or region name")
                    )
                )
            )
        )
    )
)

// 3. Use the model with function calling
suspend fun chatWithAgent(userMessage: String): String {
    val chat = model.startChat()
    
    var response = chat.sendMessage(userMessage)
    
    // Check if Gemini wants to call a function
    response.functionCalls.forEach { functionCall ->
        when (functionCall.name) {
            "getCropInfo" -> {
                val cropName = functionCall.args["cropName"] as String
                val season = functionCall.args["season"] as String
                val result = getCropInfo(cropName, season)
                
                // Send function result back to Gemini
                response = chat.sendMessage(
                    content {
                        functionResponse(functionCall.name, mapOf("result" to result))
                    }
                )
            }
            "getWeatherForecast" -> {
                val location = functionCall.args["location"] as String
                val result = getWeatherForecast(location)
                
                response = chat.sendMessage(
                    content {
                        functionResponse(functionCall.name, mapOf("result" to result))
                    }
                )
            }
        }
    }
    
    return response.text ?: "No response"
}

// 4. Example usage
// User: "What should I plant in Punjab this winter?"
// Gemini internally calls: getCropInfo("wheat", "winter") + getWeatherForecast("Punjab")
// Gemini responds: "Based on the weather and season, I recommend planting wheat..."
```

**How it works:**
1. You declare what tools exist and their parameters
2. User asks a question
3. Gemini decides which tool(s) to call based on the question
4. You execute the actual Kotlin function
5. Send results back to Gemini
6. Gemini formulates a natural language response

The agent intelligence comes from Gemini deciding WHEN and WHICH tools to use. You just provide the tools and execute them.

## Agent Use Cases Based on User Stories

### 🎯 High-Impact Agent Tasks

#### 1. Proactive Farm Monitoring Agent
**Automation:**
- Auto-analyze farm conditions daily (weather + crop stage + season)
- Send smart reminders: "Your wheat is 45 days old, apply fertilizer this week"
- Predict risks: "Heavy rain in 2 days + your tomatoes are flowering = fungal risk. Apply preventive spray"
- Track growth milestones: Auto-update crop stage, notify harvest time

**Tools Needed:**
- `getFarmDetails()` - Get crop type, sowing date, farm size
- `getWeatherForecast()` - Fetch weather data
- `calculateCropStage()` - Determine growth stage
- `createReminder()` - Schedule tasks
- `sendNotification()` - Alert user

#### 2. Disease Detection + Treatment Agent
**Automation:**
- User uploads leaf photo → Agent:
  - Identifies disease via image AI
  - Automatically searches local pesticide availability
  - Calculates dosage based on farm size
  - Checks weather - "Don't spray today, rain expected"
  - Saves to history and tracks if treatment worked

**Tools Needed:**
- `analyzePlantImage()` - Disease detection
- `searchPesticides()` - Find treatments
- `calculateDosage()` - Based on farm size
- `getWeatherForecast()` - Check spray conditions
- `saveDiseaseHistory()` - Track treatments

#### 3. Weather-Action Agent (Most Valuable!)
**Automation:**
- Don't just show weather, tell them what to do:
  - "38°C tomorrow → Water crops at 6 AM, not afternoon"
  - "Frost tonight → Cover seedlings or move indoors"
  - "No rain for 7 days → Switch to drip irrigation"
- Auto-schedule tasks in dashboard based on forecast

**Tools Needed:**
- `getWeatherForecast()` - Multi-day forecast
- `analyzeCropRisk()` - Weather impact on crops
- `generateActionPlan()` - Create specific tasks
- `scheduleTask()` - Add to dashboard
- `sendAlert()` - Urgent notifications

#### 4. Smart Chatbot Agent (Agri-Bot)
**Automation:**
- Goes beyond Q&A:
  - Context-aware: "You grow wheat in Punjab, here's localized advice"
  - Multi-step guidance: "Want to switch to organic? Here's a 3-month plan..."
  - Calls tools: Check prices, find nearby suppliers, calculate costs
  - Learns from history: "Last time you had this issue, you used X. Want to try again?"

**Tools Needed:**
- `getUserContext()` - Farm details, location, history
- `searchCropDatabase()` - Crop information
- `calculateCosts()` - Financial planning
- `findSuppliers()` - Local resources
- `getConversationHistory()` - Context memory

#### 5. Marketplace Intelligence Agent
**Automation:**
- Price alerts: "Tomato prices up 20% in your area, good time to sell"
- Auto-match buyers/sellers: "Someone nearby needs wheat seeds, you posted seeds yesterday"
- Negotiate helper: "Similar produce selling for ₹X, suggest pricing ₹Y"
- Fraud detection: Flag suspicious listings

**Tools Needed:**
- `getMarketPrices()` - Current prices
- `analyzePriceTrends()` - Historical data
- `matchBuyerSeller()` - Connect users
- `suggestPricing()` - Pricing recommendations
- `validateListing()` - Fraud detection

#### 6. Community Assistant Agent
**Automation:**
- Auto-answer common questions in farmer network
- Summarize long discussions: "5 farmers recommend this pesticide for your issue"
- Connect similar farmers: "3 others near you grow rice, want to connect?"
- Translate posts to local language

**Tools Needed:**
- `searchCommunityPosts()` - Find relevant discussions
- `summarizeThread()` - Extract key insights
- `findSimilarFarmers()` - Match by location/crop
- `translateText()` - Language support
- `generateResponse()` - Auto-reply

## 🔥 Implementation Priority

### Phase 1: Foundation (Week 1-2)
1. **AgentService** - Core orchestration layer
2. **ToolRegistry** - Tool management system
3. **ConversationManager** - Memory/context handling

### Phase 2: High-Value Agents (Week 3-4)
1. **Weather-Action Agent** - Highest immediate value, prevents crop loss
2. **Disease Detection Agent** - Solves critical pain point with automation
3. **Smart Chatbot with Tools** - Reduces expert dependency, always available

### Phase 3: Enhanced Features (Week 5-6)
4. **Proactive Farm Monitoring Agent** - Daily automation
5. **Marketplace Intelligence Agent** - Income optimization

### Phase 4: Community (Week 7-8)
6. **Community Assistant Agent** - Network engagement

## Success Metrics

### Technical Metrics
- Agent response time < 3 seconds
- Tool execution success rate > 95%
- Context retention across sessions
- Multi-turn conversation support

### User Metrics
- Reduced time to get answers (from minutes to seconds)
- Increased task completion rate
- Higher user engagement with proactive alerts
- Reduced crop loss from weather/disease

## Next Steps

1. Review and approve this plan
2. Set up development environment
3. Create AgentService foundation
4. Implement first agent (Weather-Action recommended)
5. Test with real user scenarios
6. Iterate based on feedback

---

**Document Created:** November 29, 2025  
**Status:** Planning Phase  
**Ready to Build:** Yes
