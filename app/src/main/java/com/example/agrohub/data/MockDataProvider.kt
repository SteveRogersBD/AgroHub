package com.example.agrohub.data

import android.net.Uri
import com.example.agrohub.models.*
import com.example.agrohub.ui.icons.AgroHubIcons
import com.google.android.gms.maps.model.LatLng
import kotlin.random.Random

/**
 * Mock data provider for AgroHub application.
 * Provides sample data generators for all screens and features.
 * 
 * This is a UI-only implementation with no backend logic.
 * All data is generated for demonstration and testing purposes.
 */
object MockDataProvider {
    
    // ========== Farm Data ==========
    
    /**
     * Generate sample farm data with various health statuses
     */
    fun generateFarmData(): List<FarmData> {
        return listOf(
            FarmData(
                id = "farm_1",
                name = "Green Valley Farm",
                cropType = "Wheat",
                location = LatLng(28.6139, 77.2090), // Delhi coordinates
                size = 5.5,
                healthStatus = "Healthy",
                healthPercentage = 92f
            ),
            FarmData(
                id = "farm_2",
                name = "Sunrise Orchards",
                cropType = "Rice",
                location = LatLng(28.6200, 77.2150),
                size = 8.2,
                healthStatus = "Good",
                healthPercentage = 78f
            ),
            FarmData(
                id = "farm_3",
                name = "Golden Harvest Fields",
                cropType = "Corn",
                location = LatLng(28.6050, 77.2000),
                size = 12.0,
                healthStatus = "Needs Attention",
                healthPercentage = 45f
            ),
            FarmData(
                id = "farm_4",
                name = "Riverside Plantation",
                cropType = "Cotton",
                location = LatLng(28.6250, 77.2200),
                size = 6.8,
                healthStatus = "Critical",
                healthPercentage = 28f
            ),
            FarmData(
                id = "farm_5",
                name = "Highland Crops",
                cropType = "Sugarcane",
                location = LatLng(28.6100, 77.2100),
                size = 10.5,
                healthStatus = "Excellent",
                healthPercentage = 95f
            )
        )
    }
    
    /**
     * Generate quick statistics for dashboard
     */
    fun generateQuickStats(): QuickStats {
        return QuickStats(
            totalFarms = 5,
            activeCrops = 12,
            pendingTasks = 8
        )
    }
    
    // ========== Disease Detection Data ==========
    
    /**
     * Generate sample disease results with treatments
     */
    fun generateDiseaseResults(): List<DiseaseResult> {
        return listOf(
            DiseaseResult(
                diseaseName = "Leaf Blight",
                severity = 0.65f,
                severityLevel = "Moderate",
                imageUri = Uri.parse("android.resource://com.example.agrohub/drawable/sample_crop"),
                description = "Leaf blight is a fungal disease that causes brown spots on leaves, " +
                        "leading to reduced photosynthesis and crop yield. Early detection and " +
                        "treatment are crucial for preventing spread.",
                treatments = listOf(
                    Treatment(
                        title = "Fungicide Application",
                        steps = listOf(
                            "Apply copper-based fungicide spray",
                            "Repeat application every 7-10 days",
                            "Ensure complete leaf coverage",
                            "Apply during early morning or evening"
                        ),
                        icon = AgroHubIcons.Fertilizer
                    ),
                    Treatment(
                        title = "Cultural Practices",
                        steps = listOf(
                            "Remove and destroy infected leaves",
                            "Improve air circulation between plants",
                            "Avoid overhead irrigation",
                            "Maintain proper plant spacing"
                        ),
                        icon = AgroHubIcons.Plant
                    ),
                    Treatment(
                        title = "Organic Treatment",
                        steps = listOf(
                            "Apply neem oil solution",
                            "Use baking soda spray (1 tbsp per gallon)",
                            "Apply compost tea to boost plant immunity",
                            "Mulch around plants to prevent soil splash"
                        ),
                        icon = AgroHubIcons.Leaf
                    )
                ),
                preventionTips = listOf(
                    "Plant disease-resistant varieties",
                    "Rotate crops annually to break disease cycle",
                    "Maintain proper soil drainage",
                    "Avoid working with plants when wet",
                    "Sanitize tools between uses",
                    "Monitor plants regularly for early signs",
                    "Maintain balanced soil nutrition",
                    "Remove crop debris after harvest"
                )
            ),
            DiseaseResult(
                diseaseName = "Powdery Mildew",
                severity = 0.35f,
                severityLevel = "Mild",
                imageUri = Uri.parse("android.resource://com.example.agrohub/drawable/sample_crop"),
                description = "Powdery mildew appears as white powdery spots on leaves and stems. " +
                        "It thrives in warm, dry conditions with high humidity at night.",
                treatments = listOf(
                    Treatment(
                        title = "Sulfur Treatment",
                        steps = listOf(
                            "Apply sulfur-based fungicide",
                            "Spray every 10-14 days",
                            "Avoid application in hot weather (above 90°F)"
                        ),
                        icon = AgroHubIcons.Fertilizer
                    )
                ),
                preventionTips = listOf(
                    "Ensure good air circulation",
                    "Avoid overhead watering",
                    "Plant in full sun locations",
                    "Remove infected plant parts promptly"
                )
            ),
            DiseaseResult(
                diseaseName = "Root Rot",
                severity = 0.85f,
                severityLevel = "Severe",
                imageUri = Uri.parse("android.resource://com.example.agrohub/drawable/sample_crop"),
                description = "Root rot is caused by waterlogged soil conditions leading to fungal " +
                        "infection of roots. Plants show wilting, yellowing, and stunted growth.",
                treatments = listOf(
                    Treatment(
                        title = "Drainage Improvement",
                        steps = listOf(
                            "Improve soil drainage immediately",
                            "Reduce watering frequency",
                            "Apply fungicide to soil",
                            "Consider raised bed planting"
                        ),
                        icon = AgroHubIcons.Water
                    )
                ),
                preventionTips = listOf(
                    "Ensure proper soil drainage",
                    "Avoid overwatering",
                    "Use well-draining soil mix",
                    "Plant in raised beds if needed"
                )
            )
        )
    }
    
    // ========== Weather Data ==========
    
    /**
     * Generate sample weather forecasts and alerts
     */
    fun generateWeatherForecast(): List<DailyForecast> {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val conditions = listOf(
            Triple("Sunny", AgroHubIcons.Sun, RiskLevel.LOW),
            Triple("Partly Cloudy", AgroHubIcons.Weather, RiskLevel.LOW),
            Triple("Cloudy", AgroHubIcons.Weather, RiskLevel.MEDIUM),
            Triple("Rainy", AgroHubIcons.Rain, RiskLevel.HIGH),
            Triple("Stormy", AgroHubIcons.Rain, RiskLevel.HIGH),
            Triple("Clear", AgroHubIcons.Sun, RiskLevel.LOW),
            Triple("Windy", AgroHubIcons.Wind, RiskLevel.MEDIUM)
        )
        
        return days.mapIndexed { index, day ->
            val (condition, icon, risk) = conditions[index]
            val tempHigh = (28 + Random.nextInt(-5, 8)).toString()
            val tempLow = (18 + Random.nextInt(-3, 5)).toString()
            
            DailyForecast(
                date = day,
                icon = icon,
                tempHigh = "${tempHigh}°C",
                tempLow = "${tempLow}°C",
                riskLevel = risk,
                condition = condition
            )
        }
    }
    
    /**
     * Generate sample weather alerts
     */
    fun generateWeatherAlerts(): List<WeatherAlert> {
        return listOf(
            WeatherAlert(
                alertType = "Heavy Rainfall Warning",
                severity = "High",
                description = "Heavy rainfall expected in the next 48 hours. Ensure proper drainage " +
                        "and postpone irrigation activities. Risk of waterlogging in low-lying areas.",
                timestamp = "2 hours ago",
                icon = AgroHubIcons.Rain
            ),
            WeatherAlert(
                alertType = "Heat Wave Advisory",
                severity = "Medium",
                description = "Temperatures expected to rise above 40°C for the next 3 days. " +
                        "Increase irrigation frequency and provide shade for sensitive crops.",
                timestamp = "5 hours ago",
                icon = AgroHubIcons.Temperature
            ),
            WeatherAlert(
                alertType = "Frost Warning",
                severity = "High",
                description = "Frost conditions expected tonight. Cover sensitive plants and " +
                        "consider using frost protection methods for young crops.",
                timestamp = "1 day ago",
                icon = AgroHubIcons.Temperature
            ),
            WeatherAlert(
                alertType = "Strong Wind Alert",
                severity = "Medium",
                description = "Wind speeds up to 50 km/h expected. Secure loose equipment and " +
                        "check support structures for tall crops.",
                timestamp = "3 hours ago",
                icon = AgroHubIcons.Wind
            )
        )
    }
    
    // ========== Chat Data ==========
    
    /**
     * Generate sample chat messages for Agri-Bot
     */
    fun generateChatMessages(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "msg_1",
                text = "Hello! I'm your Agri-Bot assistant. How can I help you today?",
                isUser = false,
                timestamp = "10:00 AM",
                icon = AgroHubIcons.Chat
            ),
            ChatMessage(
                id = "msg_2",
                text = "What's the best time to plant wheat in North India?",
                isUser = true,
                timestamp = "10:02 AM",
                icon = null
            ),
            ChatMessage(
                id = "msg_3",
                text = "The best time to plant wheat in North India is from mid-October to " +
                        "mid-November. This timing ensures the crop gets optimal temperature " +
                        "during germination and growth phases. The ideal soil temperature for " +
                        "wheat sowing is 20-25°C.",
                isUser = false,
                timestamp = "10:02 AM",
                icon = AgroHubIcons.Plant
            ),
            ChatMessage(
                id = "msg_4",
                text = "How much water does wheat need?",
                isUser = true,
                timestamp = "10:05 AM",
                icon = null
            ),
            ChatMessage(
                id = "msg_5",
                text = "Wheat typically requires 4-6 irrigations during its growing season, " +
                        "depending on soil type and rainfall. Critical stages for irrigation are:\n\n" +
                        "1. Crown root initiation (20-25 days after sowing)\n" +
                        "2. Tillering stage\n" +
                        "3. Jointing stage\n" +
                        "4. Flowering stage\n" +
                        "5. Milk stage\n" +
                        "6. Dough stage\n\n" +
                        "Each irrigation should provide about 5-7 cm of water.",
                isUser = false,
                timestamp = "10:05 AM",
                icon = AgroHubIcons.Water
            ),
            ChatMessage(
                id = "msg_6",
                text = "Thank you! That's very helpful.",
                isUser = true,
                timestamp = "10:08 AM",
                icon = null
            ),
            ChatMessage(
                id = "msg_7",
                text = "You're welcome! Feel free to ask if you have any more questions about " +
                        "farming, crop management, or agricultural practices. I'm here to help!",
                isUser = false,
                timestamp = "10:08 AM",
                icon = AgroHubIcons.Chat
            )
        )
    }
    
    // ========== Community Data ==========
    
    /**
     * Generate sample community posts with images
     */
    fun generateCommunityPosts(): List<Post> {
        return listOf(
            Post(
                id = "post_1",
                userName = "Rajesh Kumar",
                userAvatar = android.R.drawable.ic_menu_gallery,
                timestamp = "2 hours ago",
                content = "Just harvested my first organic wheat crop! The yield is amazing. " +
                        "Thanks to everyone in this community for the valuable advice on organic " +
                        "farming practices. 🌾",
                images = listOf(
                    android.R.drawable.ic_menu_gallery,
                    android.R.drawable.ic_menu_gallery
                ),
                likes = 124,
                comments = 18,
                shares = 5,
                isLiked = true
            ),
            Post(
                id = "post_2",
                userName = "Priya Sharma",
                userAvatar = android.R.drawable.ic_menu_gallery,
                timestamp = "5 hours ago",
                content = "Does anyone have experience with drip irrigation systems? I'm planning " +
                        "to install one for my vegetable farm. Looking for recommendations on " +
                        "reliable suppliers and installation tips.",
                images = emptyList(),
                likes = 45,
                comments = 32,
                shares = 2,
                isLiked = false
            ),
            Post(
                id = "post_3",
                userName = "Amit Patel",
                userAvatar = android.R.drawable.ic_menu_gallery,
                timestamp = "1 day ago",
                content = "Beautiful sunrise at the farm today! There's nothing quite like the " +
                        "peace and beauty of early morning in the fields. 🌅",
                images = listOf(android.R.drawable.ic_menu_gallery),
                likes = 289,
                comments = 42,
                shares = 15,
                isLiked = true
            ),
            Post(
                id = "post_4",
                userName = "Sunita Devi",
                userAvatar = android.R.drawable.ic_menu_gallery,
                timestamp = "1 day ago",
                content = "Successfully implemented integrated pest management on my cotton farm. " +
                        "Reduced pesticide use by 60% and still maintaining healthy crops. " +
                        "Happy to share my experience with anyone interested!",
                images = listOf(
                    android.R.drawable.ic_menu_gallery,
                    android.R.drawable.ic_menu_gallery,
                    android.R.drawable.ic_menu_gallery
                ),
                likes = 156,
                comments = 67,
                shares = 28,
                isLiked = false
            ),
            Post(
                id = "post_5",
                userName = "Vikram Singh",
                userAvatar = android.R.drawable.ic_menu_gallery,
                timestamp = "2 days ago",
                content = "Warning to farmers in Punjab region: Heavy rains predicted for next week. " +
                        "Make sure to harvest mature crops and secure equipment. Stay safe everyone!",
                images = emptyList(),
                likes = 203,
                comments = 89,
                shares = 45,
                isLiked = true
            ),
            Post(
                id = "post_6",
                userName = "Meera Reddy",
                userAvatar = android.R.drawable.ic_menu_gallery,
                timestamp = "3 days ago",
                content = "Attended an amazing workshop on sustainable farming practices today. " +
                        "Learned so much about soil health and crop rotation. Knowledge is power! 📚",
                images = listOf(android.R.drawable.ic_menu_gallery),
                likes = 178,
                comments = 23,
                shares = 12,
                isLiked = false
            )
        )
    }
    
    /**
     * Generate sample comments for posts
     */
    fun generateComments(): List<Comment> {
        return listOf(
            Comment(
                id = "comment_1",
                userName = "Arjun Verma",
                userAvatar = android.R.drawable.ic_menu_gallery,
                timestamp = "1 hour ago",
                content = "Congratulations! Your hard work paid off. What variety did you plant?",
                likes = 12
            ),
            Comment(
                id = "comment_2",
                userName = "Kavita Joshi",
                userAvatar = android.R.drawable.ic_menu_gallery,
                timestamp = "45 minutes ago",
                content = "This is inspiring! I'm planning to switch to organic farming next season.",
                likes = 8
            ),
            Comment(
                id = "comment_3",
                userName = "Ramesh Gupta",
                userAvatar = android.R.drawable.ic_menu_gallery,
                timestamp = "30 minutes ago",
                content = "Great results! Can you share more details about your fertilization schedule?",
                likes = 5
            )
        )
    }
    
    // ========== Marketplace Data ==========
    
    /**
     * Generate sample marketplace products
     */
    fun generateMarketplaceProducts(): List<Product> {
        return listOf(
            Product(
                id = "prod_1",
                title = "Premium Wheat Seeds - HD 2967",
                price = "₹2,500/quintal",
                location = "Delhi, India",
                category = "Seeds",
                imageUrl = android.R.drawable.ic_menu_gallery,
                sellerName = "Green Valley Seeds Co.",
                sellerAvatar = android.R.drawable.ic_menu_gallery,
                description = "High-yielding wheat variety suitable for irrigated conditions. " +
                        "Disease resistant with excellent grain quality. Certified seeds with " +
                        "95% germination rate. Ideal for North Indian plains.",
                sellerPhone = "+91 98765 43210"
            ),
            Product(
                id = "prod_2",
                title = "Organic Vermicompost - 50kg",
                price = "₹400/bag",
                location = "Pune, Maharashtra",
                category = "Fertilizers",
                imageUrl = android.R.drawable.ic_menu_gallery,
                sellerName = "EcoFarm Organics",
                sellerAvatar = android.R.drawable.ic_menu_gallery,
                description = "100% organic vermicompost rich in nutrients. Improves soil structure " +
                        "and water retention. Perfect for all crops. Free from chemicals and " +
                        "harmful pathogens. Bulk orders available.",
                sellerPhone = "+91 98765 43211"
            ),
            Product(
                id = "prod_3",
                title = "Rotary Tiller - 7ft Width",
                price = "₹45,000",
                location = "Ludhiana, Punjab",
                category = "Equipment",
                imageUrl = android.R.drawable.ic_menu_gallery,
                sellerName = "Punjab Agri Equipment",
                sellerAvatar = android.R.drawable.ic_menu_gallery,
                description = "Heavy-duty rotary tiller suitable for 35-50 HP tractors. " +
                        "Excellent for soil preparation and mixing crop residue. " +
                        "Durable construction with replaceable blades. 1-year warranty included.",
                sellerPhone = "+91 98765 43212"
            ),
            Product(
                id = "prod_4",
                title = "Fresh Tomatoes - Grade A",
                price = "₹25/kg",
                location = "Nashik, Maharashtra",
                category = "Produce",
                imageUrl = android.R.drawable.ic_menu_gallery,
                sellerName = "Sunrise Farms",
                sellerAvatar = android.R.drawable.ic_menu_gallery,
                description = "Fresh, organically grown tomatoes. Firm texture, bright red color. " +
                        "Perfect for wholesale buyers and retailers. Minimum order 100kg. " +
                        "Direct from farm to ensure freshness.",
                sellerPhone = "+91 98765 43213"
            ),
            Product(
                id = "prod_5",
                title = "Drip Irrigation Kit - 1 Acre",
                price = "₹35,000",
                location = "Jalgaon, Maharashtra",
                category = "Tools",
                imageUrl = android.R.drawable.ic_menu_gallery,
                sellerName = "AquaFlow Systems",
                sellerAvatar = android.R.drawable.ic_menu_gallery,
                description = "Complete drip irrigation system for 1 acre. Includes main line, " +
                        "lateral pipes, drippers, filters, and fittings. Saves up to 60% water. " +
                        "Free installation guidance. Suitable for all crops.",
                sellerPhone = "+91 98765 43214"
            ),
            Product(
                id = "prod_6",
                title = "Hybrid Corn Seeds - NK 6240",
                price = "₹3,200/bag (10kg)",
                location = "Hyderabad, Telangana",
                category = "Seeds",
                imageUrl = android.R.drawable.ic_menu_gallery,
                sellerName = "AgriTech Seeds",
                sellerAvatar = android.R.drawable.ic_menu_gallery,
                description = "High-performance hybrid corn seeds with excellent yield potential. " +
                        "Drought tolerant and disease resistant. Suitable for kharif season. " +
                        "Maturity in 95-100 days.",
                sellerPhone = "+91 98765 43215"
            ),
            Product(
                id = "prod_7",
                title = "NPK Fertilizer 19:19:19",
                price = "₹1,200/bag (50kg)",
                location = "Kanpur, Uttar Pradesh",
                category = "Fertilizers",
                imageUrl = android.R.drawable.ic_menu_gallery,
                sellerName = "FertiFarm Industries",
                sellerAvatar = android.R.drawable.ic_menu_gallery,
                description = "Balanced NPK fertilizer suitable for all crops. Water-soluble formula " +
                        "for quick absorption. Promotes healthy growth and high yields. " +
                        "Bulk discounts available.",
                sellerPhone = "+91 98765 43216"
            ),
            Product(
                id = "prod_8",
                title = "Solar Water Pump - 3HP",
                price = "₹85,000",
                location = "Rajkot, Gujarat",
                category = "Equipment",
                imageUrl = android.R.drawable.ic_menu_gallery,
                sellerName = "SolarAgri Solutions",
                sellerAvatar = android.R.drawable.ic_menu_gallery,
                description = "Efficient solar-powered water pump. No electricity bills. " +
                        "Suitable for irrigation up to 5 acres. Includes solar panels, " +
                        "controller, and pump. 5-year warranty. Government subsidy eligible.",
                sellerPhone = "+91 98765 43217"
            )
        )
    }
    
    // ========== Profile Data ==========
    
    /**
     * Generate sample profile notes
     */
    fun generateProfileNotes(): List<Note> {
        return listOf(
            Note(
                id = "note_1",
                title = "Wheat Planting Schedule",
                preview = "Reminder: Start wheat sowing from Nov 15. Prepare soil with proper " +
                        "plowing and leveling. Check seed availability...",
                timestamp = "2 days ago"
            ),
            Note(
                id = "note_2",
                title = "Fertilizer Application Plan",
                preview = "First dose: Basal application of DAP and Urea. Second dose: Top dressing " +
                        "at tillering stage. Third dose: At flowering...",
                timestamp = "1 week ago"
            ),
            Note(
                id = "note_3",
                title = "Pest Control Strategy",
                preview = "Monitor for aphids and stem borers. Use integrated pest management. " +
                        "Neem oil spray every 15 days. Chemical pesticides only if...",
                timestamp = "2 weeks ago"
            ),
            Note(
                id = "note_4",
                title = "Irrigation Schedule",
                preview = "Critical irrigation stages for wheat: Crown root initiation, tillering, " +
                        "jointing, flowering, milk stage, and dough stage...",
                timestamp = "3 weeks ago"
            ),
            Note(
                id = "note_5",
                title = "Market Price Tracking",
                preview = "Current wheat MSP: ₹2,125/quintal. Local market rates varying between " +
                        "₹2,200-2,400. Monitor prices weekly...",
                timestamp = "1 month ago"
            )
        )
    }
    
    /**
     * Generate sample activity history
     */
    fun generateActivityHistory(): List<Activity> {
        return listOf(
            Activity(
                id = "activity_1",
                description = "Added new farm: Green Valley Farm",
                timestamp = "Today, 10:30 AM",
                icon = AgroHubIcons.Add
            ),
            Activity(
                id = "activity_2",
                description = "Scanned crop for disease detection",
                timestamp = "Yesterday, 3:45 PM",
                icon = AgroHubIcons.Scan
            ),
            Activity(
                id = "activity_3",
                description = "Posted in community: Harvest update",
                timestamp = "2 days ago",
                icon = AgroHubIcons.Community
            ),
            Activity(
                id = "activity_4",
                description = "Purchased: Organic Vermicompost",
                timestamp = "3 days ago",
                icon = AgroHubIcons.Cart
            ),
            Activity(
                id = "activity_5",
                description = "Updated farm details: Riverside Plantation",
                timestamp = "5 days ago",
                icon = AgroHubIcons.Edit
            ),
            Activity(
                id = "activity_6",
                description = "Checked weather forecast",
                timestamp = "1 week ago",
                icon = AgroHubIcons.Weather
            ),
            Activity(
                id = "activity_7",
                description = "Saved note: Fertilizer Application Plan",
                timestamp = "1 week ago",
                icon = AgroHubIcons.Note
            ),
            Activity(
                id = "activity_8",
                description = "Chatted with Agri-Bot about wheat planting",
                timestamp = "2 weeks ago",
                icon = AgroHubIcons.Chat
            )
        )
    }
    
    /**
     * Generate sample settings items
     */
    fun generateSettingsItems(): List<SettingItem> {
        return listOf(
            SettingItem(
                title = "Account Settings",
                icon = AgroHubIcons.Profile,
                route = "account_settings"
            ),
            SettingItem(
                title = "Notifications",
                icon = AgroHubIcons.Notification,
                route = "notifications"
            ),
            SettingItem(
                title = "Language",
                icon = AgroHubIcons.Settings,
                route = "language"
            ),
            SettingItem(
                title = "Privacy & Security",
                icon = AgroHubIcons.Settings,
                route = "privacy"
            ),
            SettingItem(
                title = "Help & Support",
                icon = AgroHubIcons.Help,
                route = "help"
            ),
            SettingItem(
                title = "About",
                icon = AgroHubIcons.Info,
                route = "about"
            )
        )
    }
    
    // ========== Utility Functions ==========
    
    /**
     * Get a random farm from the generated list
     */
    fun getRandomFarm(): FarmData {
        return generateFarmData().random()
    }
    
    /**
     * Get a random disease result
     */
    fun getRandomDiseaseResult(): DiseaseResult {
        return generateDiseaseResults().random()
    }
    
    /**
     * Get a random product
     */
    fun getRandomProduct(): Product {
        return generateMarketplaceProducts().random()
    }
    
    /**
     * Get products by category
     */
    fun getProductsByCategory(category: String): List<Product> {
        return generateMarketplaceProducts().filter { it.category == category }
    }
    
    /**
     * Get all available product categories
     */
    fun getProductCategories(): List<String> {
        return listOf("Seeds", "Tools", "Fertilizers", "Produce", "Equipment")
    }
    
    /**
     * Get a product by ID
     */
    fun getProductById(productId: String): Product? {
        return generateMarketplaceProducts().find { it.id == productId }
    }
    
    // ========== Mock Feed Data for Demo ==========
    
    /**
     * Generate mock feed posts for hackathon demo
     * Uses drawable resources and creates realistic farming community posts
     */
    fun generateMockFeedPosts(): List<com.example.agrohub.domain.model.FeedPost> {
        val now = java.time.LocalDateTime.now()
        
        return listOf(
            com.example.agrohub.domain.model.FeedPost(
                id = 1L,
                author = com.example.agrohub.domain.model.PostAuthor(
                    id = 101L,
                    username = "rajesh_farmer",
                    avatarUrl = null  // No profile pic
                ),
                content = "Just harvested my wheat crop! Yield is looking great this season. Any tips for storage?",
                mediaUrl = "android.resource://com.example.agrohub/" + com.example.agrohub.R.drawable.crop,
                likeCount = 45,
                commentCount = 12,
                isLikedByCurrentUser = false,
                createdAt = now.minusHours(2),
                updatedAt = null
            ),
            com.example.agrohub.domain.model.FeedPost(
                id = 2L,
                author = com.example.agrohub.domain.model.PostAuthor(
                    id = 102L,
                    username = "priya_agro",
                    avatarUrl = null
                ),
                content = "Trying organic farming for the first time. Excited to see the results! Anyone have experience with organic pest control?",
                mediaUrl = "android.resource://com.example.agrohub/" + com.example.agrohub.R.drawable.field,
                likeCount = 67,
                commentCount = 23,
                isLikedByCurrentUser = true,
                createdAt = now.minusHours(5),
                updatedAt = null
            ),
            com.example.agrohub.domain.model.FeedPost(
                id = 3L,
                author = com.example.agrohub.domain.model.PostAuthor(
                    id = 103L,
                    username = "amit_crops",
                    avatarUrl = null
                ),
                content = "Weather forecast shows rain next week. Perfect timing for planting! Make sure to prepare your fields.",
                mediaUrl = null,  // No image
                likeCount = 89,
                commentCount = 34,
                isLikedByCurrentUser = true,
                createdAt = now.minusHours(8),
                updatedAt = null
            ),
            com.example.agrohub.domain.model.FeedPost(
                id = 4L,
                author = com.example.agrohub.domain.model.PostAuthor(
                    id = 104L,
                    username = "sunita_organic",
                    avatarUrl = null
                ),
                content = "My tomatoes are growing beautifully! Thanks to everyone who gave advice last month. This community is amazing!",
                mediaUrl = "android.resource://com.example.agrohub/" + com.example.agrohub.R.drawable.crop,
                likeCount = 123,
                commentCount = 45,
                isLikedByCurrentUser = false,
                createdAt = now.minusHours(12),
                updatedAt = null
            ),
            com.example.agrohub.domain.model.FeedPost(
                id = 5L,
                author = com.example.agrohub.domain.model.PostAuthor(
                    id = 105L,
                    username = "vikram_harvest",
                    avatarUrl = null
                ),
                content = "Looking for recommendations on pest control. Seeing some issues with my cotton crop. Any suggestions?",
                mediaUrl = null,
                likeCount = 34,
                commentCount = 56,
                isLikedByCurrentUser = false,
                createdAt = now.minusHours(18),
                updatedAt = null
            ),
            com.example.agrohub.domain.model.FeedPost(
                id = 6L,
                author = com.example.agrohub.domain.model.PostAuthor(
                    id = 106L,
                    username = "meera_fields",
                    avatarUrl = null
                ),
                content = "Attended a great workshop on sustainable farming today. Learned so much about soil health and crop rotation!",
                mediaUrl = "android.resource://com.example.agrohub/" + com.example.agrohub.R.drawable.field,
                likeCount = 78,
                commentCount = 19,
                isLikedByCurrentUser = true,
                createdAt = now.minusDays(1),
                updatedAt = null
            ),
            com.example.agrohub.domain.model.FeedPost(
                id = 7L,
                author = com.example.agrohub.domain.model.PostAuthor(
                    id = 107L,
                    username = "arjun_agri",
                    avatarUrl = null
                ),
                content = "First time using drip irrigation. The water savings are incredible! Highly recommend to everyone.",
                mediaUrl = null,
                likeCount = 156,
                commentCount = 67,
                isLikedByCurrentUser = false,
                createdAt = now.minusDays(1).minusHours(6),
                updatedAt = null
            ),
            com.example.agrohub.domain.model.FeedPost(
                id = 8L,
                author = com.example.agrohub.domain.model.PostAuthor(
                    id = 108L,
                    username = "kavita_farms",
                    avatarUrl = null
                ),
                content = "Market prices for rice are looking good. Time to sell! What are the prices in your area?",
                mediaUrl = null,
                likeCount = 92,
                commentCount = 41,
                isLikedByCurrentUser = true,
                createdAt = now.minusDays(2),
                updatedAt = null
            ),
            com.example.agrohub.domain.model.FeedPost(
                id = 9L,
                author = com.example.agrohub.domain.model.PostAuthor(
                    id = 109L,
                    username = "ramesh_organic",
                    avatarUrl = null
                ),
                content = "Anyone else dealing with this heat wave? My crops need extra care. Increased irrigation frequency.",
                mediaUrl = "android.resource://com.example.agrohub/" + com.example.agrohub.R.drawable.disease,
                likeCount = 67,
                commentCount = 28,
                isLikedByCurrentUser = false,
                createdAt = now.minusDays(2).minusHours(8),
                updatedAt = null
            ),
            com.example.agrohub.domain.model.FeedPost(
                id = 10L,
                author = com.example.agrohub.domain.model.PostAuthor(
                    id = 110L,
                    username = "deepak_harvest",
                    avatarUrl = null
                ),
                content = "Proud to say I am now certified in organic farming! Hard work pays off. Thank you to all my mentors!",
                mediaUrl = null,
                likeCount = 234,
                commentCount = 89,
                isLikedByCurrentUser = true,
                createdAt = now.minusDays(3),
                updatedAt = null
            )
        )
    }
    
    /**
     * Generate mock comments for demo posts
     */
    fun generateMockComments(postId: Long): List<com.example.agrohub.domain.model.Comment> {
        val now = java.time.LocalDateTime.now()
        
        return listOf(
            com.example.agrohub.domain.model.Comment(
                id = (postId * 100) + 1,
                postId = postId,
                author = com.example.agrohub.domain.model.CommentAuthor(
                    id = 201L,
                    username = "farmer_friend",
                    avatarUrl = null
                ),
                content = "Great work! Keep it up!",
                createdAt = now.minusHours(1),
                updatedAt = null
            ),
            com.example.agrohub.domain.model.Comment(
                id = (postId * 100) + 2,
                postId = postId,
                author = com.example.agrohub.domain.model.CommentAuthor(
                    id = 202L,
                    username = "agri_expert",
                    avatarUrl = null
                ),
                content = "Thanks for sharing this! Very helpful information.",
                createdAt = now.minusHours(2),
                updatedAt = null
            ),
            com.example.agrohub.domain.model.Comment(
                id = (postId * 100) + 3,
                postId = postId,
                author = com.example.agrohub.domain.model.CommentAuthor(
                    id = 203L,
                    username = "green_thumb",
                    avatarUrl = null
                ),
                content = "I had the same experience. This really works!",
                createdAt = now.minusHours(3),
                updatedAt = null
            )
        )
    }
}
