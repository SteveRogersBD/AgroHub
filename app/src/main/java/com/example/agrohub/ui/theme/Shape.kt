package com.example.agrohub.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * AgroHub Shape System
 * Defines rounded corner shapes for cards and components
 */
object AgroHubShapes {
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(16.dp)
    val extraLarge = RoundedCornerShape(24.dp)
    
    // Material3 Shapes mapping
    val Default = Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = small,
        medium = medium,
        large = large,
        extraLarge = extraLarge
    )
}
