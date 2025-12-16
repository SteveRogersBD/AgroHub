# Media Service

Media upload and management service using Google Cloud Storage.

## Features

- Upload images to Google Cloud Storage
- Delete images from storage
- JWT authentication
- File validation (type, size)
- Automatic unique filename generation
- Public URL generation

## API Endpoints

### Upload Image
```
POST /api/media/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

Body:
- file: image file (max 10MB)

Response:
{
  "url": "https://storage.googleapis.com/agro-photo-bucket/posts/123/uuid.jpg",
  "fileName": "posts/123/uuid.jpg",
  "contentType": "image/jpeg",
  "size": 123456
}
```

### Delete Image
```
DELETE /api/media/{fileName}
Authorization: Bearer <token>

Response: 204 No Content
```

## Configuration

Required environment variables:
- `GCS_PROJECT_ID`: Google Cloud project ID (agrohub-481313)
- `GCS_BUCKET_NAME`: Storage bucket name (agro-photo-bucket)
- `GCS_CREDENTIALS_PATH`: Path to service account JSON file
- `JWT_SECRET`: JWT secret for authentication

## File Restrictions

- **Max size**: 10MB
- **Allowed types**: JPEG, JPG, PNG, GIF, WebP
- **Storage path**: `posts/{userId}/{uuid}.{extension}`

## Local Development

1. Ensure you have the GCS credentials file at `./Backend/agro-service.json`
2. Run the service:
```bash
cd Backend/media-service
mvn spring-boot:run
```

3. Service will be available at `http://localhost:8087`

## Docker Deployment

The service is included in docker-compose.yml and will mount the credentials file automatically.

## Integration with Post Service

When creating a post with an image:
1. Upload image to media-service first
2. Get the returned URL
3. Include the URL in the `mediaUrl` field when creating the post

Example flow:
```
1. POST /api/media/upload → returns { "url": "https://..." }
2. POST /api/posts with { "content": "...", "mediaUrl": "https://..." }
```
