package com.example.agrohub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/**
 * Main activity for the AgroHub application.
 * Sets up the Compose UI with the AgroHubApp root composable.
 * 
 * Requirements: 10.1, 10.5
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgroHubApp()
        }
    }
}