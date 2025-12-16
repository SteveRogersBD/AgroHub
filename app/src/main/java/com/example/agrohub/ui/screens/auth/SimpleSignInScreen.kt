package com.example.agrohub.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.agrohub.ui.theme.AgroHubColors

@Composable
fun SimpleSignInScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroHubColors.BackgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Sign In",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AgroHubColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgroHubColors.DeepGreen
                )
            ) {
                Text("Go to Home (Test)")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { navController.navigate("sign_up") }
            ) {
                Text("Go to Sign Up", color = AgroHubColors.DeepGreen)
            }
        }
    }
}
