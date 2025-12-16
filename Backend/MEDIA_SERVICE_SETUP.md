# Media Service Setup Guide

This guide explains how to set up and use the media-service for image uploads with Google Cloud Storage.

## Overview

The media-service handles image uploads to Google Cloud Storage and returns public URLs that can be stored in the Post entity's `mediaUrl` field.

## Architecture Flow

```
Android App → Media Service → Google Cloud Storage
                ↓
            Returns URL
                ↓
Android App → Post Service (with mediaUrl)
```

## Setup Steps

### 1. Google Cloud Storage Configuration

Your configuration is already set:
- **Project ID**: `agrohub-481313`
- **Bucket Name**: `agro-photo-bucket`
- **Credentials**: `./Backend/agro-service.json`

### 2. Verify Bucket Permissions

Make sure your bucket is configured for public access:

1. Go to Google Cloud Console → Cloud Storage → Buckets
2. Click on `agro-photo-bucket`
3. Go to "Permissions" tab
4. Ensure `allUsers` has "Storage Object Viewer" role (for public URLs)

### 3. Running the Service

#### Local Development
```bash
cd Backend/media-service
mvn spring-boot:run
```

Service runs on: `http://localhost:8087`

#### Docker
```bash
cd Backend
docker-compose up media-service
```

The credentials file is automatically mounted in Docker.

## API Usage

### Upload Image

**Request:**
```bash
curl -X POST http://localhost:8087/api/media/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg"
```

**Response:**
```json
{
  "url": "https://storage.googleapis.com/agro-photo-bucket/posts/123/abc-123-def.jpg",
  "fileName": "posts/123/abc-123-def.jpg",
  "contentType": "image/jpeg",
  "size": 245678
}
```

### Delete Image

**Request:**
```bash
curl -X DELETE http://localhost:8087/api/media/posts/123/abc-123-def.jpg \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:** `204 No Content`

## Integration with Posts

### Creating a Post with Image

**Step 1: Upload Image**
```kotlin
// Android code
val imageUri = // selected image URI
val file = // convert URI to File
val response = mediaApi.uploadImage(file)
val imageUrl = response.url
```

**Step 2: Create Post with Image URL**
```kotlin
val postRequest = CreatePostRequest(
    content = "Check out this photo!",
    mediaUrl = imageUrl  // URL from step 1
)
val post = postApi.createPost(postRequest)
```

### Post Entity Structure

The Post entity already has the `mediaUrl` field:
```java
@Entity
public class Post {
    private Long id;
    private Long userId;
    private String content;
    private String mediaUrl;  // ← Image URL stored here
    private LocalDateTime createdAt;
    // ...
}
```

## File Restrictions

- **Maximum file size**: 10MB
- **Allowed formats**: JPEG, JPG, PNG, GIF, WebP
- **Storage structure**: `posts/{userId}/{uuid}.{extension}`

## Error Handling

### Common Errors

1. **File too large**
   - Error: "File size exceeds maximum limit of 10MB"
   - Solution: Compress image before upload

2. **Invalid file type**
   - Error: "Invalid file type. Only images are allowed"
   - Solution: Ensure file is JPEG, PNG, GIF, or WebP

3. **Authentication failed**
   - Error: 401 Unauthorized
   - Solution: Include valid JWT token in Authorization header

4. **GCS credentials not found**
   - Error: "Failed to initialize Google Cloud Storage"
   - Solution: Ensure `agro-service.json` exists at correct path

## Android Implementation Example

### 1. Add Media API Interface

```kotlin
interface MediaApi {
    @Multipart
    @POST("/api/media/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): UploadResponse
    
    @DELETE("/api/media/{fileName}")
    suspend fun deleteImage(
        @Path("fileName") fileName: String
    )
}

data class UploadResponse(
    val url: String,
    val fileName: String,
    val contentType: String,
    val size: Long
)
```

### 2. Upload Image Function

```kotlin
suspend fun uploadImage(uri: Uri): Result<String> {
    return try {
        val file = uriToFile(uri)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        
        val response = mediaApi.uploadImage(body)
        Result.success(response.url)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### 3. Create Post with Image

```kotlin
suspend fun createPostWithImage(content: String, imageUri: Uri?) {
    val mediaUrl = imageUri?.let { uri ->
        uploadImage(uri).getOrNull()
    }
    
    val request = CreatePostRequest(
        content = content,
        mediaUrl = mediaUrl
    )
    
    postApi.createPost(request)
}
```

## Testing

### Test Upload Endpoint

```bash
# Create a test image
echo "test" > test.jpg

# Upload it
curl -X POST http://localhost:8087/api/media/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test.jpg"
```

### Verify in Google Cloud Console

1. Go to Cloud Storage → Buckets → agro-photo-bucket
2. Navigate to `posts/` folder
3. You should see your uploaded images

## Monitoring

### Health Check
```bash
curl http://localhost:8087/actuator/health
```

### Logs
```bash
# Docker logs
docker logs media-service

# Local logs
# Check console output
```

## Security Notes

1. **Credentials File**: Never commit `agro-service.json` to git
2. **JWT Secret**: Use strong secret in production
3. **Bucket Access**: Consider using signed URLs for private images
4. **File Validation**: Service validates file type and size
5. **Rate Limiting**: Consider adding rate limiting for uploads

## Troubleshooting

### Service won't start
- Check if credentials file exists at correct path
- Verify GCS API is enabled in Google Cloud Console
- Check if port 8087 is available

### Upload fails
- Verify JWT token is valid
- Check file size (must be < 10MB)
- Ensure file is an image format
- Check GCS bucket permissions

### Images not accessible
- Verify bucket has public access enabled
- Check if `allUsers` has Storage Object Viewer role
- Ensure correct bucket name in configuration

## Next Steps

1. Update Android app to use media-service
2. Add image picker in CreatePostScreen
3. Upload image before creating post
4. Display images in feed using Coil or Glide
5. Add image compression before upload
6. Implement image caching in app

## API Gateway Integration

To route media-service through API Gateway, add to gateway configuration:

```yaml
# In api-gateway application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: media-service
          uri: ${MEDIA_SERVICE_URL:http://localhost:8087}
          predicates:
            - Path=/api/media/**
```

Then access via: `http://localhost:8080/api/media/upload`
