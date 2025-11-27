package com.example.agrohub.ui.navigation

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

/**
 * Unit tests for navigation components
 * 
 * Requirements: 10.1, 10.2
 */
class NavigationTest : StringSpec({
    
    "Routes object contains all required routes" {
        val routes = listOf(
            Routes.HOME,
            Routes.SCAN,
            Routes.WEATHER,
            Routes.COMMUNITY,
            Routes.MARKET,
            Routes.ADD_FARM,
            Routes.PROFILE
        )
        
        routes.forEach { route ->
            route.isNotEmpty() shouldBe true
        }
    }
    
    "Bottom navigation items contain main screens" {
        Routes.bottomNavItems shouldContain Routes.HOME
        Routes.bottomNavItems shouldContain Routes.SCAN
        Routes.bottomNavItems shouldContain Routes.WEATHER
        Routes.bottomNavItems shouldContain Routes.COMMUNITY
        Routes.bottomNavItems shouldContain Routes.MARKET
    }
    
    "Bottom navigation items count is correct" {
        Routes.bottomNavItems.size shouldBe 5
    }
    
    "Disease result route builder works correctly" {
        val diseaseId = "test-disease-123"
        val route = Routes.diseaseResult(diseaseId)
        route shouldBe "disease_result/test-disease-123"
    }
    
    "Product detail route builder works correctly" {
        val productId = "test-product-456"
        val route = Routes.productDetail(productId)
        route shouldBe "product_detail/test-product-456"
    }
})
