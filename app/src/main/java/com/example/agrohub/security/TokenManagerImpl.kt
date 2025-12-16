package com.example.agrohub.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Implementation of TokenManager using EncryptedSharedPreferences for secure token storage.
 * All operations are thread-safe using a mutex to prevent race conditions.
 *
 * @param context Application context for accessing EncryptedSharedPreferences
 */
class TokenManagerImpl(context: Context) : TokenManager {
    
    private val mutex = Mutex()
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    override suspend fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val expirationTime = System.currentTimeMillis() + (expiresIn * 1000)
                sharedPreferences.edit().apply {
                    putString(KEY_ACCESS_TOKEN, accessToken)
                    putString(KEY_REFRESH_TOKEN, refreshToken)
                    putLong(KEY_EXPIRATION_TIME, expirationTime)
                    apply()
                }
            }
        }
    }
    
    /**
     * Save username for the authenticated user
     */
    suspend fun saveUsername(username: String) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                sharedPreferences.edit().apply {
                    putString(KEY_USERNAME, username)
                    apply()
                }
            }
        }
    }
    
    /**
     * Get the saved username
     */
    suspend fun getUsername(): String? {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                sharedPreferences.getString(KEY_USERNAME, null)
            }
        }
    }
    
    override suspend fun getAccessToken(): String? {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
            }
        }
    }
    
    override suspend fun getRefreshToken(): String? {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
            }
        }
    }
    
    override suspend fun isAccessTokenValid(): Boolean {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                val accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
                val expirationTime = sharedPreferences.getLong(KEY_EXPIRATION_TIME, 0)
                val currentTime = System.currentTimeMillis()
                
                accessToken != null && expirationTime > currentTime
            }
        }
    }
    
    override suspend fun clearTokens() {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                sharedPreferences.edit().apply {
                    remove(KEY_ACCESS_TOKEN)
                    remove(KEY_REFRESH_TOKEN)
                    remove(KEY_EXPIRATION_TIME)
                    remove(KEY_USERNAME)
                    apply()
                }
            }
        }
    }
    
    companion object {
        private const val PREFS_NAME = "agrohub_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRATION_TIME = "expiration_time"
        private const val KEY_USERNAME = "username"
    }
}
