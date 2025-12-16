package com.example.agrohub.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.agrohub.data.remote.NetworkModule
import com.example.agrohub.data.remote.dto.RegisterRequestDto
import com.example.agrohub.ui.navigation.Routes
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing
import kotlinx.coroutines.launch

@Composable
fun WorkingSignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroHubColors.BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AgroHubSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp),
                tint = AgroHubColors.DeepGreen
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AgroHubColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xl))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, "Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroHubColors.DeepGreen,
                    focusedLabelColor = AgroHubColors.DeepGreen,
                    cursorColor = AgroHubColors.DeepGreen
                )
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, "Username") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroHubColors.DeepGreen,
                    focusedLabelColor = AgroHubColors.DeepGreen,
                    cursorColor = AgroHubColors.DeepGreen
                )
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            if (passwordVisible) "Hide" else "Show"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroHubColors.DeepGreen,
                    focusedLabelColor = AgroHubColors.DeepGreen,
                    cursorColor = AgroHubColors.DeepGreen
                )
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, "Confirm") },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            if (confirmPasswordVisible) "Hide" else "Show"
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroHubColors.DeepGreen,
                    focusedLabelColor = AgroHubColors.DeepGreen,
                    cursorColor = AgroHubColors.DeepGreen
                )
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
            
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AgroHubColors.CriticalRed.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.padding(AgroHubSpacing.md),
                        color = AgroHubColors.CriticalRed
                    )
                }
                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            }
            
            Button(
                onClick = {
                    when {
                        email.isBlank() || username.isBlank() || password.isBlank() -> {
                            errorMessage = "Please fill in all fields"
                        }
                        password != confirmPassword -> {
                            errorMessage = "Passwords do not match"
                        }
                        password.length < 8 -> {
                            errorMessage = "Password must be at least 8 characters"
                        }
                        else -> {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val authService = NetworkModule.provideAuthApiService(context)
                                    authService.register(RegisterRequestDto(email, username, password))
                                    
                                    // Navigate to sign in
                                    navController.navigate(Routes.SIGN_IN) {
                                        popUpTo(Routes.SIGN_UP) { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Registration failed"
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgroHubColors.DeepGreen
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AgroHubColors.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
            
            Row {
                Text("Already have an account?", color = AgroHubColors.TextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = { navController.navigate(Routes.SIGN_IN) }) {
                    Text("Sign In", color = AgroHubColors.DeepGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
