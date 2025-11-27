package com.example.agrohub.data

import com.example.agrohub.models.RiskLevel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank

/**
 * Unit tests for MockDataProvider
 * Verifies that all mock data generators produce valid data
 */
class MockDataProviderTest : StringSpec({
    
    "generateFarmData should return list of farms with valid data" {
        val farms = MockDataProvider.generateFarmData()
        
        farms.shouldNotBeEmpty()
        farms.shouldHaveSize(5)
        
        farms.forEach { farm ->
            farm.id.shouldNotBeBlank()
            farm.name.shouldNotBeBlank()
            farm.cropType.shouldNotBeBlank()
            farm.healthStatus.shouldNotBeBlank()
            farm.size shouldNotBe 0.0
            farm.healthPercentage shouldNotBe 0f
        }
    }
    
    "generateQuickStats should return valid statistics" {
        val stats = MockDataProvider.generateQuickStats()
        
        stats.totalFarms shouldBe 5
        stats.activeCrops shouldBe 12
        stats.pendingTasks shouldBe 8
    }
    
    "generateDiseaseResults should return list of disease results" {
        val results = MockDataProvider.generateDiseaseResults()
        
        results.shouldNotBeEmpty()
        results.shouldHaveSize(3)
        
        results.forEach { result ->
            result.diseaseName.shouldNotBeBlank()
            result.severityLevel.shouldNotBeBlank()
            result.description.shouldNotBeBlank()
            result.treatments.shouldNotBeEmpty()
            result.preventionTips.shouldNotBeEmpty()
        }
    }
    
    "generateWeatherForecast should return 7-day forecast" {
        val forecast = MockDataProvider.generateWeatherForecast()
        
        forecast.shouldHaveSize(7)
        
        forecast.forEach { day ->
            day.date.shouldNotBeBlank()
            day.tempHigh.shouldNotBeBlank()
            day.tempLow.shouldNotBeBlank()
            day.condition.shouldNotBeBlank()
        }
    }
    
    "generateWeatherAlerts should return list of alerts" {
        val alerts = MockDataProvider.generateWeatherAlerts()
        
        alerts.shouldNotBeEmpty()
        alerts.shouldHaveSize(4)
        
        alerts.forEach { alert ->
            alert.alertType.shouldNotBeBlank()
            alert.severity.shouldNotBeBlank()
            alert.description.shouldNotBeBlank()
            alert.timestamp.shouldNotBeBlank()
        }
    }
    
    "generateChatMessages should return conversation messages" {
        val messages = MockDataProvider.generateChatMessages()
        
        messages.shouldNotBeEmpty()
        messages.shouldHaveSize(7)
        
        messages.forEach { message ->
            message.id.shouldNotBeBlank()
            message.text.shouldNotBeBlank()
            message.timestamp.shouldNotBeBlank()
        }
    }
    
    "generateCommunityPosts should return list of posts" {
        val posts = MockDataProvider.generateCommunityPosts()
        
        posts.shouldNotBeEmpty()
        posts.shouldHaveSize(6)
        
        posts.forEach { post ->
            post.id.shouldNotBeBlank()
            post.userName.shouldNotBeBlank()
            post.content.shouldNotBeBlank()
            post.timestamp.shouldNotBeBlank()
        }
    }
    
    "generateComments should return list of comments" {
        val comments = MockDataProvider.generateComments()
        
        comments.shouldNotBeEmpty()
        comments.shouldHaveSize(3)
        
        comments.forEach { comment ->
            comment.id.shouldNotBeBlank()
            comment.userName.shouldNotBeBlank()
            comment.content.shouldNotBeBlank()
            comment.timestamp.shouldNotBeBlank()
        }
    }
    
    "generateMarketplaceProducts should return list of products" {
        val products = MockDataProvider.generateMarketplaceProducts()
        
        products.shouldNotBeEmpty()
        products.shouldHaveSize(8)
        
        products.forEach { product ->
            product.id.shouldNotBeBlank()
            product.title.shouldNotBeBlank()
            product.price.shouldNotBeBlank()
            product.location.shouldNotBeBlank()
            product.category.shouldNotBeBlank()
            product.sellerName.shouldNotBeBlank()
            product.description.shouldNotBeBlank()
            product.sellerPhone.shouldNotBeBlank()
        }
    }
    
    "generateProfileNotes should return list of notes" {
        val notes = MockDataProvider.generateProfileNotes()
        
        notes.shouldNotBeEmpty()
        notes.shouldHaveSize(5)
        
        notes.forEach { note ->
            note.id.shouldNotBeBlank()
            note.title.shouldNotBeBlank()
            note.preview.shouldNotBeBlank()
            note.timestamp.shouldNotBeBlank()
        }
    }
    
    "generateActivityHistory should return list of activities" {
        val activities = MockDataProvider.generateActivityHistory()
        
        activities.shouldNotBeEmpty()
        activities.shouldHaveSize(8)
        
        activities.forEach { activity ->
            activity.id.shouldNotBeBlank()
            activity.description.shouldNotBeBlank()
            activity.timestamp.shouldNotBeBlank()
        }
    }
    
    "generateSettingsItems should return list of settings" {
        val settings = MockDataProvider.generateSettingsItems()
        
        settings.shouldNotBeEmpty()
        settings.shouldHaveSize(6)
        
        settings.forEach { setting ->
            setting.title.shouldNotBeBlank()
            setting.route.shouldNotBeBlank()
        }
    }
    
    "getProductsByCategory should filter products correctly" {
        val seedProducts = MockDataProvider.getProductsByCategory("Seeds")
        
        seedProducts.shouldNotBeEmpty()
        seedProducts.forEach { product ->
            product.category shouldBe "Seeds"
        }
    }
    
    "getProductCategories should return all categories" {
        val categories = MockDataProvider.getProductCategories()
        
        categories.shouldHaveSize(5)
        categories shouldBe listOf("Seeds", "Tools", "Fertilizers", "Produce", "Equipment")
    }
    
    "getRandomFarm should return a valid farm" {
        val farm = MockDataProvider.getRandomFarm()
        
        farm.id.shouldNotBeBlank()
        farm.name.shouldNotBeBlank()
    }
    
    "getRandomDiseaseResult should return a valid disease result" {
        val result = MockDataProvider.getRandomDiseaseResult()
        
        result.diseaseName.shouldNotBeBlank()
        result.treatments.shouldNotBeEmpty()
    }
    
    "getRandomProduct should return a valid product" {
        val product = MockDataProvider.getRandomProduct()
        
        product.id.shouldNotBeBlank()
        product.title.shouldNotBeBlank()
    }
})
