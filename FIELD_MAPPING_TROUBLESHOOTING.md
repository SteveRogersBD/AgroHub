# Field Mapping Troubleshooting Guide

## Issue: Can't Add More Than 3 Points / Save Button Doesn't Work

### Possible Causes and Solutions

#### 1. **User Not Authenticated** (Most Likely)
The field mapping feature requires Firebase Authentication to save fields.

**Solution:**
- Make sure you're signed in to the app
- Check if Firebase Authentication is properly configured
- Look for error message in Snackbar: "Please sign in to save fields"

**To verify:**
- Check Logcat for: `FieldViewModel: Error saving field - Please sign in to save fields`

#### 2. **Map Click Not Registering**
The map might be consuming clicks for other gestures.

**Solution:**
- Make sure you're in drawing mode (should see blue control card at top)
- Tap directly on the map, not on UI elements
- Try tapping in different areas of the map
- Zoom in closer to the area you want to draw

#### 3. **Firebase Firestore Not Configured**
Firestore database might not be set up.

**Solution:**
1. Go to Firebase Console
2. Create a Firestore database
3. Set up security rules (see below)
4. Make sure `google-services.json` is up to date

#### 4. **Network Issues**
Can't connect to Firebase.

**Solution:**
- Check internet connection
- Check Logcat for network errors
- Verify Firebase project is active

## Debugging Steps

### 1. Check Logcat
Look for these messages:
```
FieldViewModel: Saving field 'FieldName' with X points
FieldViewModel: Field saved successfully
OR
FieldViewModel: Error saving field - [error message]
```

### 2. Verify Drawing Mode
When in drawing mode, you should see:
- Blue card at top with "Tap on map to draw field boundary"
- Point counter showing number of points
- Cancel and Save buttons
- Blue circles appearing where you tap

### 3. Test Authentication
Add this temporary code to check auth status:
```kotlin
LaunchedEffect(Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    println("Current user: ${user?.uid ?: "Not signed in"}")
}
```

### 4. Check Save Button State
The Save button is enabled only when:
- At least 3 points are drawn
- Field name is not blank
- Not currently saving (saveState is not Loading)

## Firebase Firestore Setup

### 1. Create Database
1. Go to Firebase Console → Firestore Database
2. Click "Create database"
3. Choose production mode or test mode
4. Select a location

### 2. Security Rules
Add these rules to allow authenticated users to save their fields:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /fields/{fieldId} {
      // Allow users to read their own fields
      allow read: if request.auth != null && 
                     resource.data.userId == request.auth.uid;
      
      // Allow users to create fields
      allow create: if request.auth != null &&
                       request.resource.data.userId == request.auth.uid;
      
      // Allow users to update/delete their own fields
      allow update, delete: if request.auth != null && 
                               resource.data.userId == request.auth.uid;
    }
  }
}
```

### 3. Test Mode (Temporary - for testing only)
If you want to test without authentication temporarily:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

**⚠️ WARNING:** Test mode allows anyone to read/write. Only use for testing!

## Common Error Messages

### "Please sign in to save fields"
- **Cause:** User not authenticated
- **Fix:** Sign in to the app first

### "Failed to save field: PERMISSION_DENIED"
- **Cause:** Firestore security rules blocking access
- **Fix:** Update Firestore security rules (see above)

### "Failed to save field: Network error"
- **Cause:** No internet connection or Firebase unreachable
- **Fix:** Check internet connection

## Quick Test

To quickly test if everything is working:

1. **Sign in** to the app
2. Go to **Map tab**
3. Tap **"Add Field"**
4. Enter name: **"Test Field"**
5. Tap **"Next"**
6. Tap **4-5 times** on the map (should see blue circles)
7. Tap **"Save"**
8. Watch for:
   - Loading spinner on Save button
   - Success message: "Field saved successfully!"
   - Drawing mode closes
   - Green polygon appears on map

## Still Not Working?

Check Logcat for detailed error messages:
```bash
adb logcat | grep -E "FieldViewModel|FieldRepository|Firebase"
```

Or in Android Studio:
- Open Logcat
- Filter by: `FieldViewModel`
- Look for error messages
