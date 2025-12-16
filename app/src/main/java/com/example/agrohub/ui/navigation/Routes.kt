package com.example.agrohub.ui.navigation

/**
 * Navigation routes for the AgroHub application.
 * Defines all screen destinations and route patterns.
 * 
 * Requirements: 10.1
 */
sealed class Routes(val route: String) {
    object SignIn : Routes("sign_in")
    object SignUp : Routes("sign_up")
    object Home : Routes("home")
    object Scan : Routes("scan")
    object Weather : Routes("weather")
    object Community : Routes("community")
    object Market : Routes("market")
    object Farm : Routes("farm")
    object Chat : Routes("chat")
    object AddFarm : Routes("add_farm")
    object Profile : Routes("profile")
    object CreatePost : Routes("create_post")
    
    // Routes with parameters
    object DiseaseResult : Routes("disease_result/{diseaseId}") {
        fun createRoute(diseaseId: String) = "disease_result/$diseaseId"
    }
    
    object ProductDetail : Routes("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    
    companion object {
        // Legacy constants for backward compatibility
        const val SIGN_IN = "sign_in"
        const val SIGN_UP = "sign_up"
        const val HOME = "home"
        const val SCAN = "scan"
        const val WEATHER = "weather"
        const val COMMUNITY = "community"
        const val MARKET = "market"
        const val FARM = "farm"
        const val CHAT = "chat"
        const val ADD_FARM = "add_farm"
        const val PROFILE = "profile"
        const val CREATE_POST = "create_post"
        const val DISEASE_RESULT = "disease_result/{diseaseId}"
        const val PRODUCT_DETAIL = "product_detail/{productId}"
        
        fun diseaseResult(diseaseId: String) = "disease_result/$diseaseId"
        fun productDetail(productId: String) = "product_detail/$productId"
        
        val bottomNavItems = listOf(HOME, SCAN, WEATHER, FARM, MARKET)
    }
}
