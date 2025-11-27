package com.example.agrohub.models

import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data model representing disease detection analysis results
 */
data class DiseaseResult(
    val diseaseName: String,
    val severity: Float,
    val severityLevel: String,
    val imageUri: Uri,
    val description: String,
    val treatments: List<Treatment>,
    val preventionTips: List<String>
)

/**
 * Data model representing a treatment option for a disease
 */
data class Treatment(
    val title: String,
    val steps: List<String>,
    val icon: ImageVector
)
