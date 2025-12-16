package com.example.agrohub.ui.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.agrohub.data.remote.NetworkModule
import com.example.agrohub.data.repository.AuthRepositoryImpl
import com.example.agrohub.domain.util.UiState
import com.example.agrohub.presentation.auth.AuthViewModel
import com.example.agrohub.ui.navigation.Routes
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubSpacing

/**
 * Sign Up Screen - User registration UI
 * 
 * Features:
 * - Email, username, password input fields
 * - Password visibility toggle
 * - Sign up button
 * - Navigation to sign in
 * - Fade-in animation
 */
@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    
    // Create ViewModel with dependencies - use rememberSaveable to survive config changes
    val viewModel: AuthViewModel = remember(context) {
        try {
            val tokenManager = NetworkModule.provideTokenManager(context)
            val authApiService = NetworkModule.provideAuthApiService(context)
            val authRepository = AuthRepositoryImpl(authApiService, tokenManager)
            AuthViewModel(authRepository)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    val registerState by viewModel.registerState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    // Handle registration success
    LaunchedEffect(registerState) {
        if (registerState is UiState.Success) {
            // Navigate to sign in after successful registration
            navController.navigate(Routes.SIGN_IN) {
                popUpTo(Routes.SIGN_UP) { inclusive = true }
            }
        }
    }
    
    // Animation state
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "screen_alpha"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroHubColors.BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
                .verticalScroll(rememberScrollState())
                .padding(AgroHubSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(AgroHubSpacing.xl))
            
            // Logo/Icon
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "AgroHub Logo",
                modifier = Modifier.size(80.dp),
                tint = AgroHubColors.DeepGreen
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // Title
            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AgroHubColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Subtitle
            Text(
                text = "Join the AgroHub community",
                fontSize = 16.sp,
                color = AgroHubColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xl))
            
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                placeholder = { Text("Enter your email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroHubColors.DeepGreen,
                    unfocusedBorderColor = AgroHubColors.SurfaceLight,
                    focusedLabelColor = AgroHubColors.DeepGreen,
                    focusedLeadingIconColor = AgroHubColors.DeepGreen,
                    unfocusedLeadingIconColor = AgroHubColors.TextSecondary,
                    cursorColor = AgroHubColors.DeepGreen
                )
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                placeholder = { Text("Choose a username") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Username"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroHubColors.DeepGreen,
                    unfocusedBorderColor = AgroHubColors.SurfaceLight,
                    focusedLabelColor = AgroHubColors.DeepGreen,
                    focusedLeadingIconColor = AgroHubColors.DeepGreen,
                    unfocusedLeadingIconColor = AgroHubColors.TextSecondary,
                    cursorColor = AgroHubColors.DeepGreen
                )
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                placeholder = { Text("Create a password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroHubColors.DeepGreen,
                    unfocusedBorderColor = AgroHubColors.SurfaceLight,
                    focusedLabelColor = AgroHubColors.DeepGreen,
                    focusedLeadingIconColor = AgroHubColors.DeepGreen,
                    unfocusedLeadingIconColor = AgroHubColors.TextSecondary,
                    focusedTrailingIconColor = AgroHubColors.DeepGreen,
                    unfocusedTrailingIconColor = AgroHubColors.TextSecondary,
                    cursorColor = AgroHubColors.DeepGreen
                )
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter your password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirm Password"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroHubColors.DeepGreen,
                    unfocusedBorderColor = AgroHubColors.SurfaceLight,
                    focusedLabelColor = AgroHubColors.DeepGreen,
                    focusedLeadingIconColor = AgroHubColors.DeepGreen,
                    unfocusedLeadingIconColor = AgroHubColors.TextSecondary,
                    focusedTrailingIconColor = AgroHubColors.DeepGreen,
                    unfocusedTrailingIconColor = AgroHubColors.TextSecondary,
                    cursorColor = AgroHubColors.DeepGreen
                )
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xl))
            
            // Error Messages
            if (passwordError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AgroHubColors.WarningYellow.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = passwordError!!,
                        modifier = Modifier.padding(AgroHubSpacing.md),
                        color = AgroHubColors.WarningYellow,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            }
            
            if (registerState is UiState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AgroHubColors.CriticalRed.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = (registerState as UiState.Error).message ?: "Registration failed",
                        modifier = Modifier.padding(AgroHubSpacing.md),
                        color = AgroHubColors.CriticalRed,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            }
            
            // Sign Up Button
            Button(
                onClick = {
                    passwordError = null
                    
                    when {
                        email.isBlank() || username.isBlank() || password.isBlank() -> {
                            passwordError = "Please fill in all fields"
                        }
                        password != confirmPassword -> {
                            passwordError = "Passwords do not match"
                        }
                        password.length < 8 -> {
                            passwordError = "Password must be at least 8 characters"
                        }
                        else -> {
                            viewModel.register(email, username, password)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgroHubColors.DeepGreen,
                    contentColor = AgroHubColors.White
                ),
                enabled = registerState !is UiState.Loading
            ) {
                if (registerState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AgroHubColors.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
            
            // Divider with "OR"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = AgroHubColors.SurfaceLight
                )
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = AgroHubSpacing.md),
                    color = AgroHubColors.TextSecondary,
                    fontSize = 14.sp
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = AgroHubColors.SurfaceLight
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
            
            // Sign In Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    color = AgroHubColors.TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(AgroHubSpacing.xs))
                TextButton(
                    onClick = { 
                        navController.navigate("sign_in") {
                            popUpTo("sign_up") { inclusive = true }
                        }
                    }
                ) {
                    Text(
                        text = "Sign In",
                        color = AgroHubColors.DeepGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xl))
        }
    }
}
