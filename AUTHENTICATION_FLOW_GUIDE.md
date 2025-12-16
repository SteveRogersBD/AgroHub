# Authentication Flow Guide

## Overview
The app now starts at the Sign In screen and checks authentication status to determine the initial route.

## Changes Made

### 1. Start Destination Logic
- **Not Authenticated**: App starts at Sign In screen
- **Authenticated**: App starts at Home screen (if valid token exists)

### 2. Authentication Check
The app checks for a valid access token in `TokenManager`:
```kotlin
val isAuthenticated = tokenManager.getAccessToken()?.isNotBlank() == true
val startDestination = if (isAuthenticated) Routes.HOME else Routes.SIGN_IN
```

## User Flow

### First Time Users
1. **App Opens** → Sign In Screen
2. **Tap "Sign Up"** → Sign Up Screen
3. **Fill Details** → Create Account
4. **Redirected** → Sign In Screen
5. **Enter Credentials** → Sign In
6. **Success** → Home Screen with Bottom Navigation

### Returning Users (Authenticated)
1. **App Opens** → Home Screen (token still valid)
2. **Full Access** → All features available

### Returning Users (Token Expired)
1. **App Opens** → Sign In Screen (token invalid/expired)
2. **Sign In Again** → Home Screen

## Sign In Screen Features
- Email input field
- Password input field (with show/hide toggle)
- Sign In button (disabled until both fields filled)
- Loading indicator during authentication
- Error messages for failed login
- "Sign Up" link for new users

## Sign Up Screen Features
- Email input field
- Username input field
- Password input field (with show/hide toggle)
- Confirm Password field
- Sign Up button (disabled until all fields valid)
- Loading indicator during registration
- Error messages for failed registration
- "Sign In" link for existing users

## Navigation After Authentication

### After Successful Sign In:
```kotlin
navController.navigate(Routes.HOME) {
    popUpTo(Routes.SIGN_IN) { inclusive = true }
}
```
- Navigates to Home
- Removes Sign In from back stack
- User cannot go back to Sign In with back button

### After Successful Sign Up:
```kotlin
navController.navigate(Routes.SIGN_IN) {
    popUpTo(Routes.SIGN_UP) { inclusive = true }
}
```
- Navigates to Sign In
- Removes Sign Up from back stack
- User can now sign in with new credentials

## Token Management

### Token Storage
- Access token stored in encrypted SharedPreferences
- Refresh token stored securely
- Token expiration time tracked

### Token Validation
- Checked on app start
- Checked before API calls
- Auto-refresh if expired (if refresh token valid)

## Testing the Flow

### Test Case 1: New User
1. Launch app → Should see Sign In screen
2. Tap "Sign Up"
3. Fill in details:
   - Email: test@example.com
   - Username: testuser
   - Password: Test123!
   - Confirm Password: Test123!
4. Tap "Sign Up"
5. Should redirect to Sign In
6. Enter credentials
7. Should navigate to Home

### Test Case 2: Existing User
1. Launch app → Should see Sign In screen
2. Enter credentials
3. Tap "Sign In"
4. Should navigate to Home
5. Close app
6. Reopen app → Should go directly to Home (if token valid)

### Test Case 3: Token Expiration
1. Sign in successfully
2. Wait for token to expire (or manually clear token)
3. Close and reopen app
4. Should see Sign In screen again

### Test Case 4: Field Mapping with Auth
1. Sign in successfully
2. Navigate to Map tab (Farm icon in bottom nav)
3. Tap "Add Field"
4. Enter field name
5. Draw boundaries (tap 3+ times)
6. Tap "Save"
7. Should save successfully (no "Please sign in" error)
8. Field should appear on map

## Backend Requirements

### Sign Up Endpoint
```
POST /api/auth/register
Body: {
  "email": "user@example.com",
  "username": "username",
  "password": "password"
}
Response: {
  "message": "User registered successfully"
}
```

### Sign In Endpoint
```
POST /api/auth/login
Body: {
  "email": "user@example.com",
  "password": "password"
}
Response: {
  "accessToken": "jwt_token_here",
  "refreshToken": "refresh_token_here",
  "expiresIn": 3600
}
```

## JWT Token Structure

The JWT token should contain:
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "userId": "user_id_here",
  "exp": 1234567890
}
```

This username is extracted and used for Firebase Realtime Database field storage.

## Troubleshooting

### Issue: App always starts at Sign In
- **Cause**: Token expired or invalid
- **Fix**: Sign in again

### Issue: "Please sign in to save fields"
- **Cause**: Token not found or invalid
- **Fix**: 
  1. Sign out (if option available)
  2. Sign in again
  3. Try saving field again

### Issue: Sign In button not working
- **Cause**: Backend not running or network error
- **Fix**: 
  1. Check backend is running
  2. Check network connection
  3. Check Logcat for error messages

### Issue: Token not persisting
- **Cause**: TokenManager not saving properly
- **Fix**: Check SharedPreferences implementation

## Security Notes

1. **Token Storage**: Tokens stored in encrypted SharedPreferences
2. **Password**: Never stored locally, only sent to backend
3. **HTTPS**: Should use HTTPS in production
4. **Token Expiration**: Tokens expire after set time
5. **Refresh Token**: Used to get new access token without re-login

## Future Enhancements

- [ ] Biometric authentication
- [ ] Remember me option
- [ ] Forgot password flow
- [ ] Email verification
- [ ] Social login (Google, Facebook)
- [ ] Auto-logout on token expiration
- [ ] Session management
- [ ] Multi-device support
