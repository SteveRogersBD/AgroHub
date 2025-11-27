package com.example.agrohub.data

/**
 * Demonstration of MockDataProvider usage
 * This file shows how to use the MockDataProvider in screens and components
 */
object MockDataProviderDemo {
    
    /**
     * Example: Using farm data in HomeScreen
     */
    fun exampleFarmDataUsage() {
        // Get all farms
        val farms = MockDataProvider.generateFarmData()
        println("Total farms: ${farms.size}")
        
        // Get quick stats
        val stats = MockDataProvider.generateQuickStats()
        println("Stats: ${stats.totalFarms} farms, ${stats.activeCrops} crops, ${stats.pendingTasks} tasks")
        
        // Get a random farm
        val randomFarm = MockDataProvider.getRandomFarm()
        println("Random farm: ${randomFarm.name}")
    }
    
    /**
     * Example: Using disease results in DiseaseDetectionScreen
     */
    fun exampleDiseaseDataUsage() {
        // Get all disease results
        val results = MockDataProvider.generateDiseaseResults()
        println("Available disease results: ${results.size}")
        
        // Get a specific disease result
        val result = results.first()
        println("Disease: ${result.diseaseName}, Severity: ${result.severityLevel}")
        println("Treatments: ${result.treatments.size}")
        println("Prevention tips: ${result.preventionTips.size}")
    }
    
    /**
     * Example: Using weather data in WeatherScreen
     */
    fun exampleWeatherDataUsage() {
        // Get 7-day forecast
        val forecast = MockDataProvider.generateWeatherForecast()
        println("7-day forecast available")
        
        // Get weather alerts
        val alerts = MockDataProvider.generateWeatherAlerts()
        println("Active alerts: ${alerts.size}")
    }
    
    /**
     * Example: Using chat messages in ChatScreen
     */
    fun exampleChatDataUsage() {
        // Get chat conversation
        val messages = MockDataProvider.generateChatMessages()
        println("Chat messages: ${messages.size}")
        
        // Count user vs bot messages
        val userMessages = messages.count { it.isUser }
        val botMessages = messages.count { !it.isUser }
        println("User: $userMessages, Bot: $botMessages")
    }
    
    /**
     * Example: Using community posts in CommunityScreen
     */
    fun exampleCommunityDataUsage() {
        // Get all posts
        val posts = MockDataProvider.generateCommunityPosts()
        println("Community posts: ${posts.size}")
        
        // Get comments
        val comments = MockDataProvider.generateComments()
        println("Comments: ${comments.size}")
    }
    
    /**
     * Example: Using marketplace products in MarketplaceScreen
     */
    fun exampleMarketplaceDataUsage() {
        // Get all products
        val products = MockDataProvider.generateMarketplaceProducts()
        println("Total products: ${products.size}")
        
        // Get products by category
        val categories = MockDataProvider.getProductCategories()
        println("Categories: ${categories.joinToString()}")
        
        val seedProducts = MockDataProvider.getProductsByCategory("Seeds")
        println("Seed products: ${seedProducts.size}")
    }
    
    /**
     * Example: Using profile data in ProfileScreen
     */
    fun exampleProfileDataUsage() {
        // Get notes
        val notes = MockDataProvider.generateProfileNotes()
        println("Saved notes: ${notes.size}")
        
        // Get activity history
        val activities = MockDataProvider.generateActivityHistory()
        println("Activities: ${activities.size}")
        
        // Get settings
        val settings = MockDataProvider.generateSettingsItems()
        println("Settings items: ${settings.size}")
    }
}
