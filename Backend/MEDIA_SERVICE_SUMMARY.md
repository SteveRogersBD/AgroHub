# Media Service - Quick Summary

## What Was Created

A complete microservice for handling image uploads to Google Cloud Storage.

## Service Details

- **Name**: media-service
- **Port**: 8087
- **Technology**: Spring Boot 3.2.0, Java 21
- **Storage**: Google Cloud Storage
- **Authentication**: JWT

## Your Configuration

```yaml
Project ID: agrohub-481313
Bucket Name: agro-photo-bucket
Credentials: ./Backend/agro-service.json
```

## Key Features

✅ Upload images (JPEG, PNG, GIF, WebP)  
✅ Max file size: 10MB  
✅ Automatic unique filename generation  
✅ Returns public Google Cloud Storage URLs  
✅ Delete images  
✅ JWT authentication  
✅ File validation  
✅ Docker support  

## API Endpoints

### Upload Image
```
POST /api/media/upload
Authorization: Bearer <token>
Body: multipart/form-data with "file" field

Returns:
{
  "url": "https://storage.googleapis.com/agro-photo-bucket/posts/123/uuid.jpg",
  "fileName": "posts/123/uuid.jpg",
  "contentType": "image/jpeg",
  "size": 245678
}
```

### Delete Image
```
DELETE /api/media/{fileName}
Authorization: Bearer <token>

Returns: 204 No Content
```

## How It Works with Posts

1. **User selects image** in Android app
2. **Upload to media-service** → Get URL
3. **Create post** with `mediaUrl` field containing the URL
4. **Post entity** already has `mediaUrl` field ready
5. **Display in feed** using the URL

## Post Entity (Already Updated)

```java
@Entity
public class Post {
    private Long id;
    private Long userId;
    private String content;
    private String mediaUrl;  // ← Stores image URL
    private LocalDateTime createdAt;
    // ...
}
```

## Running the Service

### Local Development
```bash
cd Backend/media-service
mvn spring-boot:run
```

### Docker
```bash
cd Backend
docker-compose up media-service
```

### All Services
```bash
cd Backend
docker-compose up
```

## Files Created

```
Backend/
├── media-service/
│   ├── src/main/java/com/socialmedia/media/
│   │   ├── MediaServiceApplication.java
│   │   ├── config/
│   │   │   ├── GcsConfig.java
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   └── MediaController.java
│   │   ├── service/
│   │   │   └── MediaService.java
│   │   ├── security/
│   │   │   └── JwtAuthenticationFilter.java
│   │   └── dto/
│   │       ├── UploadResponse.java
│   │       └── ErrorResponse.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── pom.xml
│   ├── Dockerfile
│   ├── .env.example
│   ├── .gitignore
│   ├── README.md
│   └── API_EXAMPLES.md
├── MEDIA_SERVICE_SETUP.md
├── ANDROID_IMAGE_UPLOAD_GUIDE.md
└── docker-compose.yml (updated)
```

## Next Steps

### Backend (Done ✅)
- [x] Media service created
- [x] Google Cloud Storage configured
- [x] Upload endpoint working
- [x] Delete endpoint working
- [x] Docker configuration
- [x] Post entity has mediaUrl field

### Android (To Do)
1. Add MediaApi interface
2. Create MediaRepository
3. Update CreatePostScreen with image picker
4. Add image preview
5. Upload image before creating post
6. Display images in feed using Coil
7. Add image compression (optional)

## Testing

### Test Upload
```bash
curl -X POST http://localhost:8087/api/media/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test.jpg"
```

### Test Health
```bash
curl http://localhost:8087/actuator/health
```

## Documentation

- **Setup Guide**: `Backend/MEDIA_SERVICE_SETUP.md`
- **Android Guide**: `Backend/ANDROID_IMAGE_UPLOAD_GUIDE.md`
- **API Examples**: `Backend/media-service/API_EXAMPLES.md`
- **Service README**: `Backend/media-service/README.md`

## Important Notes

1. **Credentials**: The `agro-service.json` file is already in `.gitignore`
2. **Bucket Access**: Make sure your GCS bucket has public access enabled
3. **JWT Secret**: Use the same JWT secret across all services
4. **Port**: Media service uses port 8087 (feed-service moved to 8089)
5. **File Size**: Max 10MB per image (configurable in application.yml)

## Architecture

```
Android App
    ↓
[1] Upload Image → Media Service → Google Cloud Storage
    ↓                                      ↓
[2] Get URL ←──────────────────────────────┘
    ↓
[3] Create Post (with URL) → Post Service → Database
    ↓
[4] View Feed → Feed Service → Returns posts with image URLs
    ↓
[5] Display Images (Coil loads from GCS URLs)
```

## Quick Start

1. **Verify credentials file exists**:
   ```bash
   ls Backend/agro-service.json
   ```

2. **Start the service**:
   ```bash
   cd Backend
   docker-compose up media-service
   ```

3. **Test it**:
   ```bash
   curl http://localhost:8087/actuator/health
   ```

4. **Integrate in Android** (see ANDROID_IMAGE_UPLOAD_GUIDE.md)

## Support

- Service logs: `docker logs media-service`
- Health check: `http://localhost:8087/actuator/health`
- Swagger UI: `http://localhost:8087/swagger-ui.html`
- GCS Console: https://console.cloud.google.com/storage/browser/agro-photo-bucket

---

**Status**: ✅ Backend Complete - Ready for Android Integration
