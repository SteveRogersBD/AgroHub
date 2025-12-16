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
 * Sign In Screen - User login UI
 * 
 * Features:
 * - Email and password input fields
 * - Password visibility toggle
 * - Remember me checkbox
 * - Forgot password link
 * - Sign in button
 * - Navigation to sign up
 * - Fade-in animation
 */
@Composable
fun SignInScreen(navController: NavController) {
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
    
    val loginState by viewModel.loginState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    
    // Handle login success
    LaunchedEffect(loginState) {
        if (loginState is UiState.Success) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.SIGN_IN) { inclusive = true }
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
                modifier = Modifier.size(100.dp),
                tint = AgroHubColors.DeepGreen
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
            
            // Title
            Text(
                text = "Welcome Back",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AgroHubColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Subtitle
            Text(
                text = "Sign in to continue to AgroHub",
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
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
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
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Remember Me and Forgot Password Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Remember Me Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AgroHubColors.DeepGreen,
                            uncheckedColor = AgroHubColors.TextSecondary
                        )
                    )
                    Text(
                        text = "Remember me",
                        color = AgroHubColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }
                
                // Forgot Password Link
                TextButton(
                    onClick = { /* TODO: Handle forgot password */ }
                ) {
                    Text(
                        text = "Forgot Password?",
                        color = AgroHubColors.DeepGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
            
            // Error Message
            if (loginState is UiState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AgroHubColors.CriticalRed.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = (loginState as UiState.Error).message ?: "Login failed",
                        modifier = Modifier.padding(AgroHubSpacing.md),
                        color = AgroHubColors.CriticalRed,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            }
            
            // Sign In Button
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        viewModel.login(email, password)
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
                enabled = loginState !is UiState.Loading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (loginState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AgroHubColors.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign In",
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
            
            // Social Sign In Buttons (Optional - UI only)
            OutlinedButton(
                onClick = { /* TODO: Handle Google sign in */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AgroHubColors.TextPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(AgroHubSpacing.sm))
                Text(
                    text = "Continue with Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
            
            // Sign Up Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    color = AgroHubColors.TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(AgroHubSpacing.xs))
                TextButton(
                    onClick = { 
                        navController.navigate("sign_up") {
                            popUpTo("sign_in") { inclusive = false }
                        }
                    }
                ) {
                    Text(
                        text = "Sign Up",
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
