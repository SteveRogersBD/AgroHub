package com.example.agrohub

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.agrohub.ui.navigation.AgroHubNavigation
import com.example.agrohub.ui.theme.AgroHubTheme

/**
 * Root composable for the AgroHub application.
 * Wraps the entire app with the AgroHubTheme and sets up navigation.
 * 
 * This is the main entry point for the Compose UI hierarchy.
 * 
 * Requirements: 10.1, 10.5
 * 
 * @param modifier Optional modifier for the root container
 * @param navController Optional NavController for testing purposes
 */
@Composable
fun AgroHubApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    AgroHubTheme {
        AgroHubNavigation(
            modifier = modifier.fillMaxSize(),
            navController = navController
        )
    }
}
