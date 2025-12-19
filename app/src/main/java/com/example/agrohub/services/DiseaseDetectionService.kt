package com.example.agrohub.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.agrohub.BuildConfig
import com.example.agrohub.models.DiseaseDetectionInput
import com.example.agrohub.models.DiseaseDetectionResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Service for disease detection using Gemini AI
 */
class DiseaseDetectionService(private val context: Context) {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    /**
     * Analyze crop disease using Gemini AI
     */
    suspend fun detectDisease(input: DiseaseDetectionInput): DiseaseDetectionResult {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = loadBitmapFromUri(input.imageUri)
                
                val prompt = buildPrompt(input)
                
                val response = model.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                
                val responseText = response.text ?: throw Exception("No response from AI")
                
                parseResponse(responseText, input)
            } catch (e: Exception) {
                throw Exception("Disease detection failed: ${e.message}")
            }
        }
    }

    private fun buildPrompt(input: DiseaseDetectionInput): String {
        return """
            You are an expert plant pathologist. Analyze this crop image and provide a detailed disease diagnosis.
            
            Crop Information:
            - Crop Name: ${input.cropName}
            - Crop Type: ${input.cropType.displayName}
            - Growth Stage: ${input.growthStage.displayName}
            - Affected Area: ${input.affectedArea.displayName}
            - Observed Symptoms: ${input.symptoms}
            - Additional Information: ${input.additionalInfo}
            
            Please provide your analysis in the following EXACT format (use these exact section headers):
            
            DISEASE_NAME:
            [Provide the specific disease name]
            
            DESCRIPTION:
            [Provide a 50-word description of the disease]
            
            SYMPTOMS:
            - [Symptom 1]
            - [Symptom 2]
            - [Symptom 3]
            
            POTENTIAL_THREATS:
            - [Threat 1]
            - [Threat 2]
            - [Threat 3]
            
            PREVENTION:
            - [Prevention method 1]
            - [Prevention method 2]
            - [Prevention method 3]
            
            TREATMENT:
            - [Treatment step 1]
            - [Treatment step 2]
            - [Treatment step 3]
            
            POST_DISEASE_MANAGEMENT:
            - [Management step 1]
            - [Management step 2]
            - [Management step 3]
            
            Be specific and practical in your recommendations.
        """.trimIndent()
    }

    private fun parseResponse(responseText: String, input: DiseaseDetectionInput): DiseaseDetectionResult {
        val sections = mutableMapOf<String, String>()
        var currentSection = ""
        val lines = responseText.lines()
        
        for (line in lines) {
            when {
                line.startsWith("DISEASE_NAME:") -> currentSection = "DISEASE_NAME"
                line.startsWith("DESCRIPTION:") -> currentSection = "DESCRIPTION"
                line.startsWith("SYMPTOMS:") -> currentSection = "SYMPTOMS"
                line.startsWith("POTENTIAL_THREATS:") -> currentSection = "POTENTIAL_THREATS"
                line.startsWith("PREVENTION:") -> currentSection = "PREVENTION"
                line.startsWith("TREATMENT:") -> currentSection = "TREATMENT"
                line.startsWith("POST_DISEASE_MANAGEMENT:") -> currentSection = "POST_DISEASE_MANAGEMENT"
                line.isNotBlank() && currentSection.isNotEmpty() -> {
                    sections[currentSection] = sections.getOrDefault(currentSection, "") + line + "\n"
                }
            }
        }
        
        fun extractList(section: String): List<String> {
            return sections[section]?.lines()
                ?.filter { it.trim().startsWith("-") }
                ?.map { it.trim().removePrefix("-").trim() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()
        }
        
        val diseaseName = sections["DISEASE_NAME"]?.trim() ?: "Unknown Disease"
        val description = sections["DESCRIPTION"]?.trim() ?: "No description available"
        
        return DiseaseDetectionResult(
            diseaseName = diseaseName,
            description = description,
            symptoms = extractList("SYMPTOMS"),
            potentialThreats = extractList("POTENTIAL_THREATS"),
            prevention = extractList("PREVENTION"),
            treatment = extractList("TREATMENT"),
            postDiseaseManagement = extractList("POST_DISEASE_MANAGEMENT"),
            imageUri = input.imageUri,
            inputData = input
        )
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
            ?: throw Exception("Failed to load image")
    }
}
