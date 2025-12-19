package com.example.agrohub.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data model for disease detection input
 */
@Parcelize
data class DiseaseDetectionInput(
    val imageUri: Uri,
    val cropName: String,
    val cropType: CropType,
    val growthStage: GrowthStage,
    val affectedArea: AffectedArea,
    val symptoms: String,
    val additionalInfo: String
) : Parcelable

/**
 * Crop types
 */
enum class CropType(val displayName: String) {
    FRUIT("Fruit"),
    VEGETABLE("Vegetable"),
    GRAIN("Grain"),
    LEGUME("Legume"),
    ROOT("Root Crop"),
    HERB("Herb"),
    OTHER("Other")
}

/**
 * Growth stages
 */
enum class GrowthStage(val displayName: String) {
    SEEDLING("Seedling"),
    VEGETATIVE("Vegetative"),
    BUD_FORMATION("Bud Formation"),
    FLOWERING("Flowering"),
    POLLINATION("Pollination"),
    FRUIT_SET("Fruit Set"),
    MATURITY("Maturity"),
    HARVEST("Harvest")
}

/**
 * Affected areas of the plant
 */
enum class AffectedArea(val displayName: String) {
    LEAVES("Leaves"),
    STEM("Stem"),
    ROOTS("Roots"),
    FLOWERS("Flowers"),
    FRUITS("Fruits"),
    BRANCHES("Branches"),
    WHOLE_PLANT("Whole Plant"),
    BARK("Bark"),
    SEEDS("Seeds")
}

/**
 * Disease detection result from Gemini AI
 */
@Parcelize
data class DiseaseDetectionResult(
    val diseaseName: String,
    val description: String,
    val symptoms: List<String>,
    val potentialThreats: List<String>,
    val prevention: List<String>,
    val treatment: List<String>,
    val postDiseaseManagement: List<String>,
    val imageUri: Uri,
    val inputData: DiseaseDetectionInput
) : Parcelable
