# Location Permission & App Start Changes

## Summary
Modified the app to start directly from MainActivity (HOME screen) for testing purposes and implemented automatic location permission handling with SharedPreferences storage.

## Changes Made

### 1. Created LocationPermissionManager
**File**: `app/src/main/java/com/example/agrohub/utils/LocationPermissionManager.kt`

A utility class that manages location permission state and user location data in SharedPreferences:
- `saveLocationPermissionGranted(Boolean)` - Stores permission status
- `isLocationPermissionGranted()` - Checks if permission was previously granted
- `saveUserLocation(latitude, longitude)` - Stores user's coordinates
- `getUserLocation()` - Retrieves saved location

### 2. Updated MainActivity
**File**: `app/src/main/java/com/example/agrohub/MainActivity.kt`

Enhanced MainActivity to handle location permissions on app startup:
- Requests location permission once on first launch
- Stores permission status in SharedPreferences
- Fetches and saves user location when permission is granted
- Uses Google Play Services FusedLocationProviderClient for accurate location
- Shows toast message if permission is denied
- Skips permission request on subsequent launches if already granted

### 3. Changed Start Destination
**File**: `app/src/main/java/com/example/agrohub/ui/navigation/AgroHubNavigation.kt`

Changed the navigation start destination from `Routes.SIGN_IN` to `Routes.HOME` to bypass authentication for testing.

### 4. Added Dependencies
**Files**: `app/build.gradle.kts` and `gradle/libs.versions.toml`

Added Google Play Services Location dependency (version 21.3.0) for location functionality.

## How It Works

1. **First Launch**: 
   - App starts and goes directly to HOME screen
   - Location permission dialog appears automatically
   - If granted, location is fetched and saved to SharedPreferences
   - Permission status is saved

2. **Subsequent Launches**:
   - App checks SharedPreferences for permission status
   - If already granted, no dialog is shown
   - Location is updated in background
   - User goes directly to HOME screen

## Testing Notes

- No need to sign in repeatedly during testing
- Location permission is requested only once
- Permission status persists across app restarts
- Location data is stored alongside other user details in SharedPreferences

## Permissions Required

The following permissions are already declared in AndroidManifest.xml:
- `ACCESS_FINE_LOCATION` - For precise location
- `ACCESS_COARSE_LOCATION` - For approximate location
