package com.example.agrohub.data.remote

import android.content.Context
import com.example.agrohub.BuildConfig
import com.example.agrohub.data.remote.api.AuthApiService
import com.example.agrohub.data.remote.api.CommentApiService
import com.example.agrohub.data.remote.api.FeedApiService
import com.example.agrohub.data.remote.api.FollowApiService
import com.example.agrohub.data.remote.api.LikeApiService
import com.example.agrohub.data.remote.api.NotificationApiService
import com.example.agrohub.data.remote.api.PostApiService
import com.example.agrohub.data.remote.api.UserApiService
import com.example.agrohub.data.remote.api.WeatherApiService
import com.example.agrohub.data.remote.dto.RefreshTokenRequestDto
import com.example.agrohub.data.remote.interceptor.AuthInterceptor
import com.example.agrohub.data.remote.interceptor.LoggingInterceptor
import com.example.agrohub.data.remote.interceptor.RetryInterceptor
import com.example.agrohub.data.remote.interceptor.TokenRefreshInterceptor
import com.example.agrohub.security.TokenManager
import com.example.agrohub.security.TokenManagerImpl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network module for dependency provision.
 * 
 * Provides configured instances of:
 * - Gson for JSON serialization
 * - OkHttpClient with interceptors
 * - Retrofit with base URL from BuildConfig
 * - All API service interfaces
 * 
 * Requirements: 1.1, 1.2, 1.4, 1.5
 */
object NetworkModule {
    
    // Singleton instances
    private var tokenManager: TokenManager? = null
    private var gson: Gson? = null
    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    
    /**
     * Provides configured Gson instance.
     */
    fun provideGson(): Gson {
        return gson ?: GsonBuilder().create().also { gson = it }
    }
    
    /**
     * Provides TokenManager instance.
     * Must be initialized with application context.
     */
    fun provideTokenManager(context: Context): TokenManager {
        return tokenManager ?: TokenManagerImpl(context.applicationContext)
            .also { tokenManager = it }
    }
    
    /**
     * Provides configured OkHttpClient with all interceptors.
     * 
     * Interceptors are added in the following order:
     * 1. LoggingInterceptor - logs requests/responses (debug only)
     * 2. AuthInterceptor - injects JWT tokens
     * 3. TokenRefreshInterceptor - handles 401 and refreshes tokens
     * 4. RetryInterceptor - retries failed requests
     * 
     * Timeouts are set to 30 seconds for connect, read, and write operations.
     */
    fun provideOkHttpClient(context: Context): OkHttpClient {
        return okHttpClient ?: run {
            val tokenMgr = provideTokenManager(context)
            val authService = provideAuthApiService(context)
            
            // Create refresh token callback for TokenRefreshInterceptor
            val refreshTokenCall: suspend (String) -> Pair<String, Long>? = { refreshToken ->
                try {
                    val request = RefreshTokenRequestDto(refreshToken)
                    val response = authService.refreshToken(request)
                    Pair(response.accessToken, response.expiresIn)
                } catch (e: Exception) {
                    null
                }
            }
            
            OkHttpClient.Builder()
                .addInterceptor(LoggingInterceptor(BuildConfig.DEBUG))
                .addInterceptor(AuthInterceptor(tokenMgr))
                .addInterceptor(TokenRefreshInterceptor(tokenMgr, refreshTokenCall))
                .addInterceptor(RetryInterceptor(maxRetries = 3, initialDelayMillis = 1000))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
                .also { okHttpClient = it }
        }
    }
    
    /**
     * Provides configured Retrofit instance.
     * 
     * Base URL is read from BuildConfig.BACKEND_BASE_URL which is configured
     * in local.properties. Defaults to http://10.0.2.2:8080/api for emulator.
     */
    fun provideRetrofit(context: Context): Retrofit {
        return retrofit ?: Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(provideOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create(provideGson()))
            .build()
            .also { retrofit = it }
    }
    
    // API Service Providers
    
    fun provideAuthApiService(context: Context): AuthApiService {
        // For auth service, we need a special retrofit without token refresh interceptor
        // to avoid circular dependency when refreshing tokens
        val tokenMgr = provideTokenManager(context)
        
        val authOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(LoggingInterceptor(BuildConfig.DEBUG))
            .addInterceptor(AuthInterceptor(tokenMgr))
            .addInterceptor(RetryInterceptor(maxRetries = 3, initialDelayMillis = 1000))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val authRetrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(authOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create(provideGson()))
            .build()
        
        return authRetrofit.create(AuthApiService::class.java)
    }
    
    fun provideUserApiService(context: Context): UserApiService {
        return provideRetrofit(context).create(UserApiService::class.java)
    }
    
    fun provideFollowApiService(context: Context): FollowApiService {
        return provideRetrofit(context).create(FollowApiService::class.java)
    }
    
    fun providePostApiService(context: Context): PostApiService {
        return provideRetrofit(context).create(PostApiService::class.java)
    }
    
    fun provideCommentApiService(context: Context): CommentApiService {
        return provideRetrofit(context).create(CommentApiService::class.java)
    }
    
    fun provideLikeApiService(context: Context): LikeApiService {
        return provideRetrofit(context).create(LikeApiService::class.java)
    }
    
    fun provideFeedApiService(context: Context): FeedApiService {
        return provideRetrofit(context).create(FeedApiService::class.java)
    }
    
    fun provideNotificationApiService(context: Context): NotificationApiService {
        return provideRetrofit(context).create(NotificationApiService::class.java)
    }
    
    fun provideWeatherApiService(): WeatherApiService {
        // Weather API uses a different base URL
        val weatherRetrofit = Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/v1/")
            .client(OkHttpClient.Builder()
                .addInterceptor(LoggingInterceptor(BuildConfig.DEBUG))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build())
            .addConverterFactory(GsonConverterFactory.create(provideGson()))
            .build()
        
        return weatherRetrofit.create(WeatherApiService::class.java)
    }
    
    /**
     * Clears all cached instances. Useful for testing or when context changes.
     */
    fun clearInstances() {
        tokenManager = null
        gson = null
        okHttpClient = null
        retrofit = null
    }
}
