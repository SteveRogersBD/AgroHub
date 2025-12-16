# Frontend API Integration Fixes

## Issues Found and Fixed

### 1. Login Request Field Mismatch ✅ FIXED
**Problem:**
- Frontend was sending: `{ "email": "...", "password": "..." }`
- Backend expects: `{ "emailOrUsername": "...", "password": "..." }`

**Fix:**
- Updated `LoginRequestDto` to use `emailOrUsername` field
- Updated `AuthRepositoryImpl.login()` to pass email as `emailOrUsername`

**Files Changed:**
- `app/src/main/java/com/example/agrohub/data/remote/dto/AuthDtos.kt`
- `app/src/main/java/com/example/agrohub/data/repository/AuthRepositoryImpl.kt`

---

### 2. Login Response Structure Mismatch ✅ FIXED
**Problem:**
- Frontend expected: `tokenType` and `expiresIn` fields
- Backend returns: `accessToken`, `refreshToken`, `userId`, `email`, `username`, `role`
- Backend does NOT return `tokenType` or `expiresIn`

**Fix:**
- Updated `LoginResponseDto` to match backend response structure
- Added computed properties for `tokenType` (defaults to "Bearer") and `expiresIn` (defaults to 1 hour)
- This maintains backward compatibility with existing code

**Files Changed:**
- `app/src/main/java/com/example/agrohub/data/remote/dto/AuthDtos.kt`

---

### 3. Registration Response Structure Mismatch ✅ FIXED
**Problem:**
- Frontend expected a separate `RegisterResponseDto` with fields: `id`, `email`, `username`, `role`, `createdAt`
- Backend actually returns `LoginResponse` (same as login) with tokens

**Fix:**
- Changed `RegisterResponseDto` to be a type alias of `LoginResponseDto`
- Updated `AuthRepositoryImpl.register()` to:
  - Store tokens returned from registration
  - Map response using `userId` instead of `id`
  - Use `LocalDateTime.now()` for `createdAt` since backend doesn't return it

**Files Changed:**
- `app/src/main/java/com/example/agrohub/data/remote/dto/AuthDtos.kt`
- `app/src/main/java/com/example/agrohub/data/repository/AuthRepositoryImpl.kt`

---

## API Endpoints Verification ✅

### Base URL Configuration
- **Configured**: `http://10.0.2.2:8080/api/` (for Android Emulator)
- **Backend Running**: `http://localhost:8080` (API Gateway)
- **Mapping**: `10.0.2.2` correctly maps to host machine's localhost

### Endpoints
| Frontend Call | Backend Endpoint | Status |
|--------------|------------------|--------|
| `POST auth/register` | `POST /api/auth/register` | ✅ Correct |
| `POST auth/login` | `POST /api/auth/login` | ✅ Correct |
| `POST auth/refresh` | `POST /api/auth/refresh` | ✅ Correct |

---

## Testing Recommendations

### 1. Test Registration Flow
```kotlin
// Should now work correctly
authRepository.register(
    email = "test@example.com",
    username = "testuser",
    password = "Test123!"
)
```

### 2. Test Login Flow
```kotlin
// Should now work correctly with email or username
authRepository.login(
    email = "test@example.com", // or username
    password = "Test123!"
)
```

### 3. Verify Token Storage
After successful login/registration, verify:
- Access token is stored
- Refresh token is stored
- Tokens are included in subsequent API calls

---

## Summary

All API integration issues between the Android frontend and Spring Boot backend have been resolved:

1. ✅ Login request now sends correct field name (`emailOrUsername`)
2. ✅ Login response parsing matches backend structure
3. ✅ Registration response correctly handles tokens
4. ✅ API endpoints are correctly configured
5. ✅ Base URL properly configured for emulator

The sign-in and sign-up pages should now work correctly with the backend!
