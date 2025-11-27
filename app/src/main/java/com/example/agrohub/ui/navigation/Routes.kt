package com.example.agrohub.ui.navigation

/**
 * Navigation routes for the AgroHub application.
 * Defines all screen destinations and route patterns.
 * 
 * Requirements: 10.1
 */
object Routes {
    /**
     * Home/Dashboard screen route
     */
    const val HOME = "home"
    
    /**
     * Disease detection/scan screen route
     */
    const val SCAN = "scan"
    
    /**
     * Weather forecast screen route
     */
    const val WEATHER = "weather"
    
    /**
     * Community feed screen route
     */
    const val COMMUNITY = "community"
    
    /**
     * Marketplace screen route
     */
    const val MARKET = "market"
    
    /**
     * Farm/Field map screen route
     */
    const val FARM = "farm"
    
    /**
     * Chat/Agri-Bot screen route
     */
    const val CHAT = "chat"
    
    /**
     * Add farm screen route
     */
    const val ADD_FARM = "add_farm"
    
    /**
     * Profile screen route
     */
    const val PROFILE = "profile"
    
    /**
     * Disease result screen route with disease ID parameter
     */
    const val DISEASE_RESULT = "disease_result/{diseaseId}"
    
    /**
     * Product detail screen route with product ID parameter
     */
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    
    /**
     * Create disease result route with specific ID
     */
    fun diseaseResult(diseaseId: String) = "disease_result/$diseaseId"
    
    /**
     * Create product detail route with specific ID
     */
    fun productDetail(productId: String) = "product_detail/$productId"
    
    /**
     * Bottom navigation items - screens that appear in the bottom nav bar
     */
    val bottomNavItems = listOf(HOME, SCAN, WEATHER, FARM, MARKET)
}
