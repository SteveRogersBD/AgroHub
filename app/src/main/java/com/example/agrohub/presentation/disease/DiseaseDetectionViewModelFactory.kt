package com.example.agrohub.presentation.disease

import android.content.Context

/**
 * Factory to provide a shared instance of DiseaseDetectionViewModel
 */
object DiseaseDetectionViewModelFactory {
    private var instance: DiseaseDetectionViewModel? = null
    
    fun getInstance(context: Context): DiseaseDetectionViewModel {
        return instance ?: DiseaseDetectionViewModel(context.applicationContext).also {
            instance = it
        }
    }
    
    fun clearInstance() {
        instance = null
    }
}
