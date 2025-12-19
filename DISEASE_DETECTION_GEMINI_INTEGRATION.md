# Disease Detection with Gemini AI Integration

## Overview

The Disease Detection feature in the Scan tab now uses Google Gemini AI to analyze crop images and provide comprehensive disease diagnosis with related news articles.

## Features Implemented

### 1. Image Selection
- **Camera Capture**: Take a photo directly from the camera
- **Gallery Selection**: Choose an existing image from the device gallery

### 2. Crop Information Form
After selecting an image, users provide detailed information:

- **Crop Name** (Text Field): Enter the name of the crop (e.g., Tomato, Wheat, Rice)
- **Crop Type** (Dropdown): Select from:
  - Fruit
  - Vegetable
  - Grain
  - Legume
  - Root Crop
  - Herb
  - Other

- **Growth Stage** (Dropdown): Select from:
  - Seedling
  - Vegetative
  - Bud Formation
  - Flowering
  - Pollination
  - Fruit Set
  - Maturity
  - Harvest

- **Affected Area** (Dropdown): Select the affected part:
  - Leaves
  - Stem
  - Roots
  - Flowers
  - Fruits
  - Branches
  - Whole Plant
  - Bark
  - Seeds

- **Symptoms** (Text Field): Describe observed symptoms
- **Additional Information** (Text Field - Optional): Any other relevant details

### 3. AI-Powered Disease Analysis

The Gemini AI analyzes the image along with the provided information and returns:

#### Disease Name
The specific name of the identified disease

#### Description
A concise 50-word description of the disease

#### Symptoms
A detailed list of disease symptoms to watch for

#### Potential Threats
List of threats the disease poses to the crop

#### Prevention
Preventive measures to avoid the disease

#### Treatment
Step-by-step treatment recommendations

#### Post Disease Management
Guidelines for managing the crop after disease treatment

### 4. Related News Articles

After disease detection, the app displays related news articles in a horizontal scrollable view:
- **Large Cards**: Each article is displayed in a big, visually appealing card
- **Thumbnail Images**: High-quality images from the articles
- **Source & Date**: Article source and publication date
- **Clickable**: Tap to open the full article in a browser

## Technical Implementation

### Files Created

1. **Models**
   - `app/src/main/java/com/example/agrohub/models/DiseaseDetectionModels.kt`
     - `DiseaseDetectionInput`: Input data model
     - `CropType`, `GrowthStage`, `AffectedArea`: Enums for dropdowns
     - `DiseaseDetectionResult`: Result data model

2. **Services**
   - `app/src/main/java/com/example/agrohub/services/DiseaseDetectionService.kt`
     - Handles Gemini AI communication
     - Image processing
     - Response parsing

3. **ViewModel**
   - `app/src/main/java/com/example/agrohub/presentation/disease/DiseaseDetectionViewModel.kt`
     - Manages UI state
     - Coordinates disease analysis
     - Fetches related news articles
   - `app/src/main/java/com/example/agrohub/presentation/disease/DiseaseDetectionViewModelFactory.kt`
     - Provides shared ViewModel instance

4. **UI Screens**
   - `app/src/main/java/com/example/agrohub/ui/screens/disease/DiseaseDetectionScreen.kt` (Updated)
     - Image selection interface
     - Crop information form
   - `app/src/main/java/com/example/agrohub/ui/screens/disease/DiseaseResultScreen.kt` (New)
     - Disease analysis results
     - Related news articles

### API Integration

#### Gemini AI
- **Model**: `gemini-2.0-flash`
- **API Key**: Loaded from `BuildConfig.GEMINI_API_KEY`
- **Features Used**:
  - Image analysis
  - Structured text generation
  - Context-aware responses

#### News API (SerpAPI)
- **Endpoint**: Google News search
- **Query**: Dynamically generated based on disease name
- **Display**: Horizontal scrollable cards

### Data Flow

1. User selects image → Opens input form
2. User fills crop information → Submits for analysis
3. ViewModel calls DiseaseDetectionService
4. Service sends image + data to Gemini AI
5. AI returns structured disease analysis
6. ViewModel fetches related news articles
7. Results displayed in DiseaseResultScreen

## Usage Instructions

### For Users

1. **Navigate to Scan Tab**
   - Tap the "Scan" icon in the bottom navigation

2. **Select Image**
   - Tap "Capture Photo" to take a new photo
   - Or tap "Choose from Gallery" to select existing image

3. **Fill Crop Information**
   - Enter crop name
   - Select crop type from dropdown
   - Select growth stage from dropdown
   - Select affected area from dropdown
   - Describe symptoms
   - Add any additional information (optional)

4. **Analyze**
   - Tap "Analyze" button
   - Wait for AI analysis (loading indicator shown)

5. **View Results**
   - Review disease name and description
   - Read symptoms, threats, prevention, and treatment
   - Scroll through related news articles
   - Tap articles to read more

6. **Return**
   - Tap back button to scan another crop

### For Developers

#### Prerequisites

1. **Gemini API Key**
   - Obtain from [Google AI Studio](https://aistudio.google.com/app/apikey)
   - Add to `local.properties`:
     ```
     gemini.api.key=YOUR_API_KEY_HERE
     ```

2. **SerpAPI Key**
   - Already configured in `NetworkModule.kt`
   - Update if needed

#### Testing

```kotlin
// Test disease detection
val input = DiseaseDetectionInput(
    imageUri = uri,
    cropName = "Tomato",
    cropType = CropType.VEGETABLE,
    growthStage = GrowthStage.VEGETATIVE,
    affectedArea = AffectedArea.LEAVES,
    symptoms = "Yellow spots on leaves",
    additionalInfo = "Recent heavy rainfall"
)

viewModel.analyzeDisease(input)
```

#### Customization

**Modify AI Prompt**:
Edit `DiseaseDetectionService.buildPrompt()` to adjust the analysis format

**Change News Query**:
Edit `DiseaseDetectionViewModel.loadRelatedNews()` to customize article search

**Update UI**:
Modify `DiseaseResultScreen.kt` for different layouts

## Error Handling

- **No Internet**: Shows error message with retry option
- **Invalid API Key**: Displays authentication error
- **Image Load Failure**: Prompts user to select another image
- **No News Found**: Shows "No related articles found" message

## Performance Considerations

- **Image Compression**: Images are loaded efficiently using Coil
- **Async Operations**: All API calls run on background threads
- **State Management**: Uses Kotlin Flow for reactive UI updates
- **Memory Management**: ViewModel survives configuration changes

## Future Enhancements

- [ ] Offline disease database for common diseases
- [ ] Save disease detection history
- [ ] Share results with other farmers
- [ ] Multi-language support
- [ ] Voice input for symptoms
- [ ] Batch image analysis
- [ ] Disease severity scoring
- [ ] Treatment product recommendations

## Troubleshooting

### Issue: "Failed to analyze disease"
- **Solution**: Check internet connection and API key

### Issue: News articles not loading
- **Solution**: Verify SerpAPI key and internet connection

### Issue: Image not displaying
- **Solution**: Ensure proper permissions for camera/storage

### Issue: Dropdowns not working
- **Solution**: Update to latest Material3 version

## Dependencies

```kotlin
// Gemini AI
implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

// Image Loading
implementation("io.coil-kt:coil-compose:2.4.0")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Kotlin Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## API Response Format

The Gemini AI returns responses in this structured format:

```
DISEASE_NAME:
[Disease name]

DESCRIPTION:
[50-word description]

SYMPTOMS:
- [Symptom 1]
- [Symptom 2]
- [Symptom 3]

POTENTIAL_THREATS:
- [Threat 1]
- [Threat 2]

PREVENTION:
- [Prevention 1]
- [Prevention 2]

TREATMENT:
- [Treatment 1]
- [Treatment 2]

POST_DISEASE_MANAGEMENT:
- [Management 1]
- [Management 2]
```

## Security Notes

- API keys stored in `local.properties` (not committed to git)
- Loaded via BuildConfig at compile time
- Never expose API keys in client-side code for production
- Consider using a backend proxy for production apps

## Support

For issues or questions:
1. Check this documentation
2. Review error logs
3. Verify API keys are configured
4. Ensure all dependencies are installed

---

**Last Updated**: December 19, 2025
**Version**: 1.0.0
**Status**: ✅ Fully Implemented
