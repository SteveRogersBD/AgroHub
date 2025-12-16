# Android JWT Integration Guide

## Overview
This guide shows how to integrate JWT authentication in your Android app after the backend refactoring.

## Architecture

```
Android App → API Gateway (validates JWT) → Microservices
```

The app only communicates with the Gateway. The Gateway handles all authentication and routing.

## Implementation Steps

### 1. Add Dependencies (build.gradle.kts)

```kotlin
dependencies {
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // OkHttp for interceptors
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // DataStore for secure storage (recommended over SharedPreferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

### 2. Create Token Manager

```kotlin
// app/src/main/java/com/example/agrohub/data/auth/TokenManager.kt
package com.example.agrohub.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {
    
    companion object {
        private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }
    
    // Save JWT token
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
        }
    }
    
    // Save refresh token
    suspend fun saveRefreshToken(refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }
    
    // Get JWT token
    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[JWT_TOKEN_KEY]
        }
    }
    
    // Get refresh token
    fun getRefreshToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }
    }
    
    // Clear tokens (logout)
    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }
}
```

### 3. Create Auth Interceptor

```kotlin
// app/src/main/java/com/example/agrohub/network/AuthInterceptor.kt
package com.example.agrohub.network

import com.example.agrohub.data.auth.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Skip adding token for login/register endpoints
        val path = request.url.encodedPath
        if (path.contains("/auth/login") || path.contains("/auth/register")) {
            return chain.proceed(request)
        }
        
        // Get token synchronously (in real app, handle this better)
        val token = runBlocking {
            tokenManager.getToken().first()
        }
        
        // Add Authorization header if token exists
        val newRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        
        return chain.proceed(newRequest)
    }
}
```

### 4. Create Token Refresh Interceptor

```kotlin
// app/src/main/java/com/example/agrohub/network/TokenRefreshInterceptor.kt
package com.example.agrohub.network

import com.example.agrohub.data.auth.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class TokenRefreshInterceptor(
    private val tokenManager: TokenManager,
    private val authApi: AuthApi
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        // If 401 Unauthorized, try to refresh token
        if (response.code == 401) {
            response.close()
            
            val refreshToken = runBlocking {
                tokenManager.getRefreshToken().first()
            }
            
            if (refreshToken != null) {
                try {
                    // Call refresh endpoint
                    val refreshResponse = runBlocking {
                        authApi.refreshToken(RefreshTokenRequest(refreshToken))
                    }
                    
                    // Save new token
                    runBlocking {
                        tokenManager.saveToken(refreshResponse.token)
                    }
                    
                    // Retry original request with new token
                    val newRequest = request.newBuilder()
                        .header("Authorization", "Bearer ${refreshResponse.token}")
                        .build()
                    
                    return chain.proceed(newRequest)
                } catch (e: Exception) {
                    // Refresh failed, clear tokens and redirect to login
                    runBlocking {
                        tokenManager.clearTokens()
                    }
                }
            }
        }
        
        return response
    }
}
```

### 5. Setup Retrofit

```kotlin
// app/src/main/java/com/example/agrohub/network/ApiClient.kt
package com.example.agrohub.network

import android.content.Context
import com.example.agrohub.data.auth.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    
    private const val BASE_URL = "http://10.0.2.2:8080/" // For Android emulator
    // Use "http://localhost:8080/" for physical device on same network
    
    fun provideOkHttpClient(context: Context): OkHttpClient {
        val tokenManager = TokenManager(context)
        
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    fun provideRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun provideAuthApi(context: Context): AuthApi {
        return provideRetrofit(context).create(AuthApi::class.java)
    }
    
    fun providePostApi(context: Context): PostApi {
        return provideRetrofit(context).create(PostApi::class.java)
    }
    
    // Add other API interfaces as needed
}
```

### 6. Create API Interfaces

```kotlin
// app/src/main/java/com/example/agrohub/network/AuthApi.kt
package com.example.agrohub.network

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
    
    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponse
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val userId: Long,
    val email: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)

data class RegisterResponse(
    val token: String,
    val refreshToken: String,
    val userId: Long,
    val email: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val token: String,
    val refreshToken: String
)
```

```kotlin
// app/src/main/java/com/example/agrohub/network/PostApi.kt
package com.example.agrohub.network

import retrofit2.http.*

interface PostApi {
    
    @GET("api/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PostListResponse
    
    @POST("api/posts")
    suspend fun createPost(@Body request: CreatePostRequest): PostResponse
    
    @GET("api/posts/{id}")
    suspend fun getPost(@Path("id") id: Long): PostResponse
    
    @DELETE("api/posts/{id}")
    suspend fun deletePost(@Path("id") id: Long)
}

data class CreatePostRequest(
    val content: String,
    val mediaUrls: List<String>? = null
)

data class PostResponse(
    val id: Long,
    val userId: Long,
    val content: String,
    val mediaUrls: List<String>,
    val createdAt: String
)

data class PostListResponse(
    val posts: List<PostResponse>,
    val totalPages: Int,
    val totalElements: Long
)
```

### 7. Use in ViewModel

```kotlin
// app/src/main/java/com/example/agrohub/ui/auth/LoginViewModel.kt
package com.example.agrohub.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrohub.data.auth.TokenManager
import com.example.agrohub.network.ApiClient
import com.example.agrohub.network.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenManager = TokenManager(application)
    private val authApi = ApiClient.provideAuthApi(application)
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            try {
                val response = authApi.login(LoginRequest(email, password))
                
                // Save tokens
                tokenManager.saveToken(response.token)
                tokenManager.saveRefreshToken(response.refreshToken)
                
                _loginState.value = LoginState.Success(response)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Login failed")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            tokenManager.clearTokens()
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val response: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}
```

### 8. Use in Composable

```kotlin
// app/src/main/java/com/example/agrohub/ui/auth/LoginScreen.kt
package com.example.agrohub.ui.auth

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val loginState by viewModel.loginState.collectAsState()
    
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
        }
    }
    
    Column {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        
        Button(
            onClick = { viewModel.login(email, password) },
            enabled = loginState !is LoginState.Loading
        ) {
            Text(if (loginState is LoginState.Loading) "Loading..." else "Login")
        }
        
        if (loginState is LoginState.Error) {
            Text(
                text = (loginState as LoginState.Error).message,
                color = Color.Red
            )
        }
    }
}
```

## Testing

### Test Login Flow
1. Run backend services
2. Run Android app
3. Try to login
4. Check Logcat for Authorization header
5. Verify JWT is stored in DataStore

### Test Protected Endpoints
1. Login successfully
2. Navigate to posts screen
3. Verify posts are loaded (JWT is automatically added)
4. Check Logcat for "Authorization: Bearer ..." header

### Test Token Refresh
1. Wait for token to expire (or manually expire it)
2. Make an API call
3. Verify token is refreshed automatically
4. Verify request succeeds with new token

## Security Best Practices

1. **Use HTTPS** in production
2. **Store tokens securely** (DataStore encrypted)
3. **Don't log tokens** in production
4. **Implement certificate pinning** for extra security
5. **Handle token expiration** gracefully
6. **Clear tokens on logout**
7. **Use refresh tokens** to minimize token exposure

## Troubleshooting

### Issue: 401 Unauthorized
- Check if JWT is being added to header
- Verify token is not expired
- Check backend logs for validation errors

### Issue: Network Error
- Verify backend is running
- Check BASE_URL (use 10.0.2.2 for emulator)
- Check network permissions in AndroidManifest.xml

### Issue: Token Not Persisting
- Verify DataStore is properly configured
- Check if tokens are being saved after login
- Use Android Studio's App Inspection to view DataStore

## Next Steps

1. Implement all API interfaces
2. Add error handling for network failures
3. Implement offline support with Room database
4. Add biometric authentication
5. Implement certificate pinning
6. Add analytics for authentication events
