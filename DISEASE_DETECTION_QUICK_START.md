# Disease Detection - Quick Start Guide

## 🚀 Quick Setup (5 minutes)

### Step 1: Ensure Gemini API Key is Configured

Your Gemini API key should already be in `local.properties`:

```properties
gemini.api.key=YOUR_GEMINI_API_KEY
```

If not, get one from [Google AI Studio](https://aistudio.google.com/app/apikey) and add it.

### Step 2: Build and Run

```bash
./gradlew clean build
./gradlew installDebug
```

Or simply run from Android Studio.

### Step 3: Test the Feature

1. Open the app
2. Tap the **Scan** tab (camera icon in bottom navigation)
3. Tap **"Choose from Gallery"**
4. Select a crop image with visible disease symptoms
5. Fill in the form:
   - **Crop Name**: e.g., "Tomato"
   - **Crop Type**: Select "Vegetable"
   - **Growth Stage**: Select "Vegetative"
   - **Affected Area**: Select "Leaves"
   - **Symptoms**: e.g., "Yellow spots with brown edges"
   - **Additional Info**: (optional)
6. Tap **"Analyze"**
7. Wait for AI analysis (2-5 seconds)
8. View comprehensive disease report
9. Scroll horizontally to see related news articles

## 📱 User Flow

```
Scan Tab
   ↓
Select Image (Camera/Gallery)
   ↓
Fill Crop Information Form
   ↓
Tap "Analyze"
   ↓
Loading Screen (AI Processing)
   ↓
Disease Result Screen
   ├── Disease Name
   ├── Description
   ├── Symptoms
   ├── Potential Threats
   ├── Prevention
   ├── Treatment
   ├── Post Disease Management
   └── Related News Articles (Horizontal Scroll)
```

## 🎯 What's New

### Input Form Features
- ✅ Crop name text field
- ✅ Crop type dropdown (7 options)
- ✅ Growth stage dropdown (8 stages)
- ✅ Affected area dropdown (9 areas)
- ✅ Symptoms text field (multi-line)
- ✅ Additional info text field (optional, multi-line)

### AI Analysis Results
- ✅ Disease name with icon
- ✅ 50-word description
- ✅ Bulleted symptoms list
- ✅ Potential threats list
- ✅ Prevention measures
- ✅ Treatment steps
- ✅ Post-disease management

### News Integration
- ✅ Related articles from Google News
- ✅ Large horizontal cards (300x350dp)
- ✅ Article thumbnails
- ✅ Source and date
- ✅ Clickable to open in browser

## 🔧 Technical Details

### New Files Created

```
models/
  └── DiseaseDetectionModels.kt          # Data models and enums

services/
  └── DiseaseDetectionService.kt         # Gemini AI integration

presentation/disease/
  ├── DiseaseDetectionViewModel.kt       # State management
  └── DiseaseDetectionViewModelFactory.kt # Shared instance

ui/screens/disease/
  ├── DiseaseDetectionScreen.kt          # Updated with form
  └── DiseaseResultScreen.kt             # New result screen
```

### Updated Files

```
ui/navigation/
  └── AgroHubNavigation.kt               # Added result screen route
```

## 🧪 Testing Scenarios

### Test Case 1: Tomato Blight
- **Crop**: Tomato
- **Type**: Vegetable
- **Stage**: Vegetative
- **Area**: Leaves
- **Symptoms**: "Dark brown spots with yellow halos on leaves"

### Test Case 2: Wheat Rust
- **Crop**: Wheat
- **Type**: Grain
- **Stage**: Flowering
- **Area**: Leaves
- **Symptoms**: "Orange-red pustules on leaf surface"

### Test Case 3: Apple Scab
- **Crop**: Apple
- **Type**: Fruit
- **Stage**: Fruit Set
- **Area**: Fruits
- **Symptoms**: "Dark olive-green spots on fruit skin"

## 📊 Expected Results

After analysis, you should see:

1. **Disease Name**: Specific disease identified
2. **Description**: ~50 words explaining the disease
3. **Symptoms**: 3-5 bullet points
4. **Threats**: 2-4 potential impacts
5. **Prevention**: 3-5 preventive measures
6. **Treatment**: 3-5 treatment steps
7. **Management**: 2-4 post-treatment tips
8. **News**: 5-10 related articles (if available)

## ⚠️ Common Issues

### Issue: "Failed to analyze disease"
**Cause**: No internet or invalid API key
**Fix**: Check connection and verify `gemini.api.key` in `local.properties`

### Issue: Form validation error
**Cause**: Required fields empty
**Fix**: Ensure Crop Name and Symptoms are filled

### Issue: No news articles
**Cause**: No matching articles or API limit
**Fix**: Normal behavior, not all diseases have recent news

### Issue: Image not loading
**Cause**: Permission denied
**Fix**: Grant camera/storage permissions in app settings

## 🎨 UI Components Used

- **Material3 Components**:
  - `OutlinedTextField` for text inputs
  - `ExposedDropdownMenuBox` for dropdowns
  - `Card` for content sections
  - `LazyRow` for horizontal news scroll
  - `CircularProgressIndicator` for loading

- **Custom Components**:
  - `PrimaryButton` for main actions
  - `SecondaryButton` for cancel actions
  - `InfoSection` for text content
  - `InfoListSection` for bulleted lists
  - `NewsArticleCard` for article display

## 📈 Performance

- **Image Analysis**: 2-5 seconds (depends on internet speed)
- **News Loading**: 1-3 seconds (parallel with analysis)
- **UI Responsiveness**: Smooth scrolling and transitions
- **Memory Usage**: Optimized with Coil image loading

## 🔐 Security

- ✅ API keys in `local.properties` (not in git)
- ✅ Loaded via BuildConfig
- ✅ HTTPS for all API calls
- ✅ No sensitive data stored locally

## 📝 Next Steps

1. **Test with real crop images**
2. **Try different crop types and diseases**
3. **Verify news articles are relevant**
4. **Check UI on different screen sizes**
5. **Test error scenarios (no internet, etc.)**

## 💡 Tips

- Use clear, well-lit images for best results
- Provide detailed symptoms for accurate diagnosis
- Check related articles for additional information
- Save important results by taking screenshots

## 🆘 Need Help?

1. Check `DISEASE_DETECTION_GEMINI_INTEGRATION.md` for detailed docs
2. Review error logs in Logcat
3. Verify all dependencies are installed
4. Ensure API keys are correctly configured

---

**Ready to test!** 🎉

Navigate to the Scan tab and start detecting crop diseases with AI!
