# Field Mapping System - Complete Guide

## Overview
Complete field mapping system using Firebase Realtime Database with automatic area calculation, reverse geocoding, and task management.

## ✅ Features Implemented

### 1. **Authentication Integration**
- Uses existing backend authentication system
- Extracts username from JWT token
- Fields are stored per username in Firebase Realtime Database

### 2. **Firebase Realtime Database Structure**
```
fields/
  └── {username}/
      └── {fieldId}/
          ├── id: string
          ├── name: string
          ├── username: string
          ├── points: array of {latitude, longitude}
          ├── centerPoint: {latitude, longitude}
          ├── centerAddress: string
          ├── areaInSquareMeters: number
          ├── tasks: array of task objects
          ├── createdAt: timestamp
          └── updatedAt: timestamp
```

### 3. **Automatic Calculations**

#### Area Calculation
- Uses Shoelace formula for polygon area
- Converts from degrees to square meters
- Accounts for latitude variation in longitude distance
- Formula: `area = Σ(lng[i] * lat[i+1] - lng[i+1] * lat[i]) / 2`

#### Center Point Calculation
- Calculates average of all boundary points
- Formula: `center = (Σlat/n, Σlng/n)`

#### Reverse Geocoding
- Uses Android Geocoder API
- Converts center coordinates to human-readable address
- Falls back to coordinates if address not found

### 4. **Field Display**
- All saved fields shown as green polygons
- Boundary points marked with green circles
- Center marker shows:
  - Field name
  - Calculated area in m²
  - Address
- Map automatically centers on most recent field
- Smooth animation when switching between fields

### 5. **Task Management Structure**
Each field can have multiple tasks with:
- `id`: Unique task identifier
- `title`: Task name
- `description`: Task details
- `status`: PENDING, IN_PROGRESS, COMPLETED, CANCELLED
- `dueDate`: Optional due date timestamp
- `createdAt`: Creation timestamp

## 🔧 Technical Implementation

### Data Models

#### Field
```kotlin
data class Field(
    val id: String,
    val name: String,
    val username: String,
    val points: List<FieldPoint>,
    val centerPoint: FieldPoint?,
    val centerAddress: String,
    val areaInSquareMeters: Double,
    val tasks: List<FieldTask>,
    val createdAt: Long,
    val updatedAt: Long
)
```

#### FieldPoint
```kotlin
data class FieldPoint(
    val latitude: Double,
    val longitude: Double
)
```

#### FieldTask
```kotlin
data class FieldTask(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val dueDate: Long?,
    val createdAt: Long
)
```

### Repository Methods

1. **saveField(field: Field)**: Saves field with all calculations
2. **getUserFields()**: Returns Flow of all user's fields (sorted by most recent)
3. **deleteField(fieldId: String)**: Deletes a specific field
4. **getUsername()**: Extracts username from JWT token
5. **getAddressFromCoordinates()**: Reverse geocoding

## 📱 User Flow

### 1. Sign In
- User must be signed in with backend authentication
- JWT token is stored and used for username extraction

### 2. View Map
- Map opens centered on most recent field (if any)
- All saved fields displayed as green polygons
- Each field shows name, area, and address on marker

### 3. Add New Field
1. Tap "Add Field" button
2. Enter field name in dialog
3. Tap "Next" to enter drawing mode
4. Tap on map to mark boundary points (minimum 3)
5. Tap "Save" when done

### 4. Automatic Processing
When saving:
1. Calculates center point from all boundary points
2. Performs reverse geocoding to get address
3. Calculates area using Shoelace formula
4. Saves to Firebase under username
5. Field appears immediately on map

## 🎯 Area Calculation Example

For a field with points:
- Point 1: (28.6139, 77.2090)
- Point 2: (28.6149, 77.2100)
- Point 3: (28.6139, 77.2100)

1. **Shoelace Formula**:
   ```
   area = |Σ(lng[i] * lat[i+1] - lng[i+1] * lat[i])| / 2
   ```

2. **Convert to meters**:
   - 1° latitude ≈ 111,320 meters
   - 1° longitude ≈ 111,320 * cos(latitude) meters

3. **Result**: Area in square meters

## 🗺️ Map Features

### Camera Behavior
- **On first load**: Centers on most recent field OR user location OR default
- **After saving**: Animates to newly saved field
- **Multiple fields**: Shows all fields, focuses on most recent

### Visual Elements
- **Saved fields**: Green polygons with 30% opacity
- **Boundary points**: Green circles (5m radius)
- **Center markers**: Show field info
- **Drawing mode**: Blue polygons and circles
- **Current drawing**: Real-time polygon preview

## 🔐 Firebase Setup

### 1. Enable Realtime Database
1. Go to Firebase Console
2. Navigate to Realtime Database
3. Click "Create Database"
4. Choose location
5. Start in test mode (for development)

### 2. Security Rules
```json
{
  "rules": {
    "fields": {
      "$username": {
        ".read": "auth != null && auth.token.username == $username",
        ".write": "auth != null && auth.token.username == $username"
      }
    }
  }
}
```

**Note**: Adjust based on your authentication token structure.

### 3. Test Mode (Development Only)
```json
{
  "rules": {
    "fields": {
      ".read": true,
      ".write": true
    }
  }
}
```

⚠️ **WARNING**: Test mode allows anyone to read/write. Only for development!

## 📊 Data Examples

### Saved Field Example
```json
{
  "fields": {
    "john_farmer": {
      "-NxYz123abc": {
        "id": "-NxYz123abc",
        "name": "North Field",
        "username": "john_farmer",
        "points": [
          {"latitude": 28.6139, "longitude": 77.2090},
          {"latitude": 28.6149, "longitude": 77.2100},
          {"latitude": 28.6139, "longitude": 77.2100}
        ],
        "centerPoint": {
          "latitude": 28.6142,
          "longitude": 77.2097
        },
        "centerAddress": "Connaught Place, New Delhi, Delhi 110001, India",
        "areaInSquareMeters": 12450.5,
        "tasks": [],
        "createdAt": 1702934400000,
        "updatedAt": 1702934400000
      }
    }
  }
}
```

## 🐛 Troubleshooting

### "Please sign in to save fields"
- **Cause**: Not authenticated or token expired
- **Fix**: Sign in again through the app

### Area shows as 0
- **Cause**: Less than 3 points
- **Fix**: Add at least 3 boundary points

### Address shows coordinates
- **Cause**: Geocoder couldn't find address or no internet
- **Fix**: Check internet connection; coordinates are shown as fallback

### Fields not appearing
- **Cause**: Firebase rules blocking access or wrong username
- **Fix**: Check Firebase rules and authentication

### Map not centering on field
- **Cause**: No centerPoint calculated
- **Fix**: Field should auto-calculate center; check field data

## 🚀 Future Enhancements (Not Yet Implemented)

### Task Management UI
- Add tasks to fields
- Mark tasks as complete
- Set due dates
- Task notifications

### Field Editing
- Edit field boundaries
- Update field name
- Modify field properties

### Advanced Features
- Field sharing between users
- Export field data (KML, GeoJSON)
- Offline support
- Field analytics
- Crop planning integration
- Weather overlay for fields

## 📝 Usage Tips

1. **Accurate Drawing**: Zoom in close before drawing boundaries
2. **Point Placement**: Tap carefully at field corners
3. **Area Accuracy**: More points = more accurate area calculation
4. **Address Quality**: Better GPS signal = better address accuracy
5. **Field Organization**: Use descriptive names for easy identification

## 🔄 Data Flow

```
User Action → ViewModel → Repository → Firebase
                ↓
            UI Update ← Flow ← Listener ← Firebase
```

1. User draws field boundaries
2. ViewModel receives points
3. Repository calculates center, area, address
4. Data saved to Firebase
5. Firebase listener triggers
6. Flow emits updated field list
7. UI updates automatically

## ✅ Testing Checklist

- [ ] Sign in successfully
- [ ] Map centers on user location or most recent field
- [ ] Can add new field with name
- [ ] Drawing mode shows blue circles
- [ ] Can add multiple points (3+)
- [ ] Save button enables after 3 points
- [ ] Field saves successfully
- [ ] Success message appears
- [ ] Field appears as green polygon
- [ ] Center marker shows correct info
- [ ] Area calculation is reasonable
- [ ] Address is retrieved
- [ ] Multiple fields all display
- [ ] Map focuses on most recent field
- [ ] Fields persist after app restart

## 📞 Support

If issues persist:
1. Check Logcat for error messages
2. Verify Firebase configuration
3. Confirm authentication is working
4. Check internet connection
5. Verify location permissions granted
