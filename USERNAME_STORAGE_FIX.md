# Username Storage Fix

## Problem
The field mapping feature was showing "Please sign in to save fields" even after signing in because the username couldn't be extracted from the JWT token.

## Root Cause
The FieldRepository was trying to decode the JWT token to extract the username, but:
1. JWT decoding is complex and error-prone
2. The token structure might vary
3. It's inefficient to decode the token every time

## Solution
Store the username directly in encrypted SharedPreferences when the user signs in.

## Changes Made

### 1. TokenManagerImpl.kt
Added two new methods:
```kotlin
suspend fun saveUsername(username: String)
suspend fun getUsername(): String?
```

- Username is stored in encrypted SharedPreferences
- Username is cleared when tokens are cleared (on logout)

### 2. WorkingSignInScreen.kt
Updated to save username after successful login:
```kotlin
val response = authService.login(LoginRequestDto(email, password))
tokenManager.saveTokens(response.accessToken, response.refreshToken, response.expiresIn)
tokenManager.saveUsername(response.username) // NEW
```

### 3. FieldRepository.kt
Simplified username retrieval:
```kotlin
private suspend fun getUsername(): String? {
    val tokenManager = NetworkModule.provideTokenManager(context) as TokenManagerImpl
    return tokenManager.getUsername()
}
```

## How It Works Now

### Sign In Flow:
1. User enters credentials
2. Backend returns LoginResponseDto with:
   - accessToken
   - refreshToken
   - **username**
   - email
   - userId
   - role
3. App saves tokens AND username to encrypted storage
4. Username is available for Firebase operations

### Field Saving Flow:
1. User draws field boundaries
2. Taps "Save"
3. FieldRepository calls `getUsername()`
4. Username retrieved from encrypted storage
5. Field saved to Firebase under: `fields/{username}/{fieldId}`

## Testing Steps

1. **Sign out** (if signed in)
2. **Sign in** with your credentials
3. Go to **Map tab**
4. Tap **"Add Field"**
5. Enter field name
6. Draw boundaries (3+ points)
7. Tap **"Save"**
8. Should see: **"Field saved successfully!"**
9. Field should appear on map

## Benefits

1. **Reliable**: Username comes directly from backend response
2. **Efficient**: No JWT decoding needed
3. **Secure**: Stored in encrypted SharedPreferences
4. **Simple**: Straightforward retrieval
5. **Consistent**: Same username used across app

## Data Storage

### Encrypted SharedPreferences Keys:
- `access_token`: JWT access token
- `refresh_token`: JWT refresh token
- `expiration_time`: Token expiration timestamp
- `username`: User's username (NEW)

### Firebase Realtime Database Structure:
```
fields/
  └── {username}/          ← Username from encrypted storage
      └── {fieldId}/
          ├── name
          ├── points
          ├── centerPoint
          ├── centerAddress
          ├── areaInSquareMeters
          └── tasks
```

## Security Notes

1. Username stored in **encrypted** SharedPreferences (not plain text)
2. Uses Android's EncryptedSharedPreferences with AES256_GCM
3. Cleared on logout
4. Only accessible within the app

## Troubleshooting

### Still seeing "Please sign in to save fields"?

**Solution**: Sign out and sign in again
- Old sessions don't have username saved
- New sign-in will save username properly

### How to force re-authentication:
1. Clear app data (Settings → Apps → AgroHub → Clear Data)
2. Or uninstall and reinstall app
3. Sign in again
4. Try saving field

### Check if username is saved:
Add this temporary code to check:
```kotlin
LaunchedEffect(Unit) {
    val tokenManager = NetworkModule.provideTokenManager(context) as TokenManagerImpl
    val username = tokenManager.getUsername()
    println("Saved username: $username")
}
```

## Future Improvements

- [ ] Add user profile screen showing username
- [ ] Allow username change
- [ ] Sync username if backend updates it
- [ ] Cache username in ViewModel for performance
- [ ] Add username validation
