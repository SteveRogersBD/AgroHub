# Android Image Upload Integration Guide

Quick guide for integrating image uploads in your Android app.

## 1. Add Dependencies

In `app/build.gradle.kts`:
```kotlin
dependencies {
    // Retrofit for multipart upload
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Image picker
    implementation("androidx.activity:activity-compose:1.8.0")
}
```

## 2. Create Media API Interface

```kotlin
// data/remote/api/MediaApi.kt
interface MediaApi {
    @Multipart
    @POST("/api/media/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): UploadResponse
    
    @DELETE("/api/media/{fileName}")
    suspend fun deleteImage(
        @Path("fileName", encoded = true) fileName: String
    ): Response<Unit>
}

data class UploadResponse(
    val url: String,
    val fileName: String,
    val contentType: String,
    val size: Long
)
```

## 3. Create Media Repository

```kotlin
// data/repository/MediaRepository.kt
class MediaRepositoryImpl(
    private val mediaApi: MediaApi,
    private val context: Context
) : MediaRepository {
    
    override suspend fun uploadImage(uri: Uri): Result<String> {
        return try {
            val file = uriToFile(uri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            val response = mediaApi.uploadImage(body)
            file.delete() // Clean up temp file
            Result.success(response.url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun uriToFile(uri: Uri): File {
        val contentResolver = context.contentResolver
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        
        contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        return tempFile
    }
}
```

## 4. Update CreatePostScreen

```kotlin
// ui/screens/CreatePostScreen.kt
@Composable
fun CreatePostScreen(
    viewModel: PostViewModel = hiltViewModel()
) {
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Content input
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("What's on your mind?") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Image picker button
        Button(
            onClick = { imagePickerLauncher.launch("image/*") }
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Photo")
        }
        
        // Show selected image
        selectedImageUri?.let { uri ->
            Spacer(modifier = Modifier.height(16.dp))
            AsyncImage(
                model = uri,
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Remove image button
            TextButton(onClick = { selectedImageUri = null }) {
                Text("Remove Image")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Post button
        Button(
            onClick = {
                isUploading = true
                viewModel.createPostWithImage(content, selectedImageUri)
            },
            enabled = content.isNotBlank() && !isUploading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("Post")
            }
        }
    }
}
```

## 5. Update PostViewModel

```kotlin
// presentation/post/PostViewModel.kt
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Idle)
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()
    
    fun createPostWithImage(content: String, imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = PostUiState.Loading
            
            try {
                // Upload image first if present
                val mediaUrl = imageUri?.let { uri ->
                    mediaRepository.uploadImage(uri).getOrNull()
                }
                
                // Create post with image URL
                val request = CreatePostRequest(
                    content = content,
                    mediaUrl = mediaUrl
                )
                
                postRepository.createPost(request)
                    .onSuccess {
                        _uiState.value = PostUiState.Success(it)
                    }
                    .onFailure {
                        _uiState.value = PostUiState.Error(it.message ?: "Failed to create post")
                    }
            } catch (e: Exception) {
                _uiState.value = PostUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class PostUiState {
    object Idle : PostUiState()
    object Loading : PostUiState()
    data class Success(val post: Post) : PostUiState()
    data class Error(val message: String) : PostUiState()
}
```

## 6. Display Images in Feed

```kotlin
// ui/screens/FeedScreen.kt
@Composable
fun PostItem(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User info
            Text(
                text = "User ${post.userId}",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Post content
            Text(text = post.content)
            
            // Post image if available
            post.mediaUrl?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = url,
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.placeholder),
                    error = painterResource(R.drawable.error_image)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Timestamp
            Text(
                text = post.createdAt.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
```

## 7. Add Permissions

In `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

## 8. Image Compression (Optional but Recommended)

```kotlin
// util/ImageCompressor.kt
object ImageCompressor {
    fun compressImage(context: Context, uri: Uri, maxSizeKB: Int = 500): File {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        
        val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        var quality = 90
        
        do {
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            quality -= 10
        } while (file.length() > maxSizeKB * 1024 && quality > 0)
        
        return file
    }
}

// Use in repository
private fun uriToFile(uri: Uri): File {
    return ImageCompressor.compressImage(context, uri, maxSizeKB = 800)
}
```

## Complete Flow

1. **User selects image** → Image picker opens
2. **Image selected** → Preview shown
3. **User clicks Post** → Image uploads to media-service
4. **Upload complete** → URL returned
5. **Create post** → Post created with mediaUrl
6. **Success** → Navigate back to feed
7. **Feed loads** → Images displayed using Coil

## Testing

```kotlin
// Test image upload
@Test
fun `upload image returns URL`() = runTest {
    val mockUri = mockk<Uri>()
    val expectedUrl = "https://storage.googleapis.com/bucket/image.jpg"
    
    coEvery { mediaApi.uploadImage(any()) } returns UploadResponse(
        url = expectedUrl,
        fileName = "image.jpg",
        contentType = "image/jpeg",
        size = 12345
    )
    
    val result = mediaRepository.uploadImage(mockUri)
    
    assertTrue(result.isSuccess)
    assertEquals(expectedUrl, result.getOrNull())
}
```

## Error Handling

```kotlin
fun createPostWithImage(content: String, imageUri: Uri?) {
    viewModelScope.launch {
        _uiState.value = PostUiState.Loading
        
        try {
            val mediaUrl = imageUri?.let { uri ->
                mediaRepository.uploadImage(uri)
                    .onFailure { error ->
                        _uiState.value = PostUiState.Error("Failed to upload image: ${error.message}")
                        return@launch
                    }
                    .getOrNull()
            }
            
            postRepository.createPost(CreatePostRequest(content, mediaUrl))
                .onSuccess { post ->
                    _uiState.value = PostUiState.Success(post)
                }
                .onFailure { error ->
                    _uiState.value = PostUiState.Error("Failed to create post: ${error.message}")
                }
        } catch (e: Exception) {
            _uiState.value = PostUiState.Error("Unexpected error: ${e.message}")
        }
    }
}
```

## Summary

✅ Media service created and configured  
✅ Post entity already has `mediaUrl` field  
✅ Upload endpoint: `POST /api/media/upload`  
✅ Returns public Google Cloud Storage URL  
✅ URL stored in post's `mediaUrl` field  
✅ Images displayed in feed using Coil  

The backend is ready! Just implement the Android code above to enable image uploads in your app.
