package com.example.agrohub.ui.icons

import androidx.compose.ui.graphics.Color
import com.example.agrohub.ui.theme.AgroHubColors
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty

/**
 * Unit tests for AgroHubIcons
 * Validates icon set completeness and consistency
 */
class AgroHubIconsTest : StringSpec({
    
    "all required agricultural icons are defined" {
        // Verify all required agricultural icons from task requirements
        AgroHubIcons.Leaf shouldNotBe null
        AgroHubIcons.Plant shouldNotBe null
        AgroHubIcons.Field shouldNotBe null
        AgroHubIcons.Sun shouldNotBe null
        AgroHubIcons.Rain shouldNotBe null
        AgroHubIcons.Disease shouldNotBe null
        AgroHubIcons.Chat shouldNotBe null
        AgroHubIcons.Cart shouldNotBe null
    }
    
    "status icons have correct color coding" {
        // Verify green for healthy status (Requirements 12.5)
        AgroHubIcons.HealthyStatus.color shouldBe AgroHubColors.MediumGreen
        
        // Verify yellow for warning status (Requirements 12.5)
        AgroHubIcons.WarningStatus.color shouldBe AgroHubColors.GoldenHarvest
        
        // Verify red for critical status (Requirements 12.5)
        AgroHubIcons.CriticalStatus.color shouldBe Color(0xFFE53935)
    }
    
    "getHealthStatusIcon returns correct status based on percentage" {
        // Test healthy range (>= 70%)
        val healthyIcon = AgroHubIcons.getHealthStatusIcon(85f)
        healthyIcon shouldBe AgroHubIcons.HealthyStatus
        
        // Test warning range (40% - 69%)
        val warningIcon = AgroHubIcons.getHealthStatusIcon(55f)
        warningIcon shouldBe AgroHubIcons.WarningStatus
        
        // Test critical range (< 40%)
        val criticalIcon = AgroHubIcons.getHealthStatusIcon(25f)
        criticalIcon shouldBe AgroHubIcons.CriticalStatus
    }
    
    "getRiskStatusIcon returns correct status based on risk level" {
        // Test LOW risk
        val lowRisk = AgroHubIcons.getRiskStatusIcon("LOW")
        lowRisk shouldBe AgroHubIcons.HealthyStatus
        
        // Test MEDIUM risk
        val mediumRisk = AgroHubIcons.getRiskStatusIcon("MEDIUM")
        mediumRisk shouldBe AgroHubIcons.WarningStatus
        
        // Test HIGH risk
        val highRisk = AgroHubIcons.getRiskStatusIcon("HIGH")
        highRisk shouldBe AgroHubIcons.CriticalStatus
        
        // Test case insensitivity
        val lowercaseRisk = AgroHubIcons.getRiskStatusIcon("low")
        lowercaseRisk shouldBe AgroHubIcons.HealthyStatus
    }
    
    "icon size constants are defined correctly" {
        // Verify icon size constants (Requirements 12.2)
        AgroHubIcons.IconSize.Small shouldBe 16f
        AgroHubIcons.IconSize.Medium shouldBe 24f
        AgroHubIcons.IconSize.Large shouldBe 32f
        AgroHubIcons.IconSize.ExtraLarge shouldBe 48f
    }
    
    "navigation icons are defined" {
        // Verify all navigation icons are present
        AgroHubIcons.Home shouldNotBe null
        AgroHubIcons.Scan shouldNotBe null
        AgroHubIcons.Weather shouldNotBe null
        AgroHubIcons.Community shouldNotBe null
        AgroHubIcons.Market shouldNotBe null
        AgroHubIcons.Profile shouldNotBe null
    }
    
    "action icons are defined" {
        // Verify common action icons are present
        AgroHubIcons.Add shouldNotBe null
        AgroHubIcons.Edit shouldNotBe null
        AgroHubIcons.Delete shouldNotBe null
        AgroHubIcons.Save shouldNotBe null
        AgroHubIcons.Share shouldNotBe null
        AgroHubIcons.Like shouldNotBe null
        AgroHubIcons.Send shouldNotBe null
    }
    
    "status icon descriptions are meaningful" {
        // Verify status icons have descriptive text
        AgroHubIcons.HealthyStatus.description.shouldNotBeEmpty()
        AgroHubIcons.WarningStatus.description.shouldNotBeEmpty()
        AgroHubIcons.CriticalStatus.description.shouldNotBeEmpty()
    }
})
