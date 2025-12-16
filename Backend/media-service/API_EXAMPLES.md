# Media Service API Examples

## Base URL
- Local: `http://localhost:8087`
- Docker: `http://localhost:8087`
- Via Gateway: `http://localhost:8080` (after gateway integration)

## Authentication

All endpoints (except health check) require JWT authentication:
```
Authorization: Bearer <your-jwt-token>
```

## Endpoints

### 1. Upload Image

Upload an image file to Google Cloud Storage.

**Endpoint:** `POST /api/media/upload`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Body (form-data):**
```
file: [binary image file]
```

**cURL Example:**
```bash
curl -X POST http://localhost:8087/api/media/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg"
```

**Success Response (200 OK):**
```json
{
  "url": "https://storage.googleapis.com/agro-photo-bucket/posts/123/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
  "fileName": "posts/123/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
  "contentType": "image/jpeg",
  "size": 245678
}
```

**Error Responses:**

File too large (400 Bad Request):
```json
{
  "message": "File size exceeds maximum limit of 10MB",
  "error": "Validation Error",
  "status": 400,
  "timestamp": "2024-12-15T10:30:00"
}
```

Invalid file type (400 Bad Request):
```json
{
  "message": "Invalid file type. Only images are allowed",
  "error": "Validation Error",
  "status": 400,
  "timestamp": "2024-12-15T10:30:00"
}
```

Unauthorized (401):
```json
{
  "message": "Unauthorized",
  "error": "Authentication Error",
  "status": 401,
  "timestamp": "2024-12-15T10:30:00"
}
```

---

### 2. Delete Image

Delete an image from Google Cloud Storage.

**Endpoint:** `DELETE /api/media/{fileName}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Path Parameters:**
- `fileName`: The file name returned from upload (URL encoded)

**cURL Example:**
```bash
curl -X DELETE "http://localhost:8087/api/media/posts/123/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Success Response (204 No Content):**
```
(empty body)
```

**Error Response (500 Internal Server Error):**
```json
{
  "message": "Failed to delete file",
  "error": "Internal Server Error",
  "status": 500,
  "timestamp": "2024-12-15T10:30:00"
}
```

---

### 3. Health Check

Check service health (no authentication required).

**Endpoint:** `GET /actuator/health`

**cURL Example:**
```bash
curl http://localhost:8087/actuator/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## Complete Workflow Example

### Creating a Post with Image

**Step 1: Get JWT Token**
```bash
# Login to get token
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 123
}
```

**Step 2: Upload Image**
```bash
curl -X POST http://localhost:8087/api/media/upload \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "file=@my-photo.jpg"

# Response:
{
  "url": "https://storage.googleapis.com/agro-photo-bucket/posts/123/uuid.jpg",
  "fileName": "posts/123/uuid.jpg",
  "contentType": "image/jpeg",
  "size": 245678
}
```

**Step 3: Create Post with Image URL**
```bash
curl -X POST http://localhost:8084/api/posts \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Check out this amazing photo!",
    "mediaUrl": "https://storage.googleapis.com/agro-photo-bucket/posts/123/uuid.jpg"
  }'

# Response:
{
  "id": 456,
  "userId": 123,
  "content": "Check out this amazing photo!",
  "mediaUrl": "https://storage.googleapis.com/agro-photo-bucket/posts/123/uuid.jpg",
  "createdAt": "2024-12-15T10:30:00",
  "updatedAt": "2024-12-15T10:30:00"
}
```

**Step 4: View Post in Feed**
```bash
curl -X GET http://localhost:8087/api/feed \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Response includes posts with images:
{
  "posts": [
    {
      "id": 456,
      "userId": 123,
      "content": "Check out this amazing photo!",
      "mediaUrl": "https://storage.googleapis.com/agro-photo-bucket/posts/123/uuid.jpg",
      "createdAt": "2024-12-15T10:30:00",
      "likesCount": 0,
      "commentsCount": 0
    }
  ]
}
```

---

## Postman Collection

Import this JSON into Postman:

```json
{
  "info": {
    "name": "Media Service API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Upload Image",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}",
            "type": "text"
          }
        ],
        "body": {
          "mode": "formdata",
          "formdata": [
            {
              "key": "file",
              "type": "file",
              "src": []
            }
          ]
        },
        "url": {
          "raw": "{{base_url}}/api/media/upload",
          "host": ["{{base_url}}"],
          "path": ["api", "media", "upload"]
        }
      }
    },
    {
      "name": "Delete Image",
      "request": {
        "method": "DELETE",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}",
            "type": "text"
          }
        ],
        "url": {
          "raw": "{{base_url}}/api/media/{{file_name}}",
          "host": ["{{base_url}}"],
          "path": ["api", "media", "{{file_name}}"]
        }
      }
    },
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "url": {
          "raw": "{{base_url}}/actuator/health",
          "host": ["{{base_url}}"],
          "path": ["actuator", "health"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8087"
    },
    {
      "key": "jwt_token",
      "value": "your-token-here"
    },
    {
      "key": "file_name",
      "value": "posts/123/uuid.jpg"
    }
  ]
}
```

---

## Testing with Different Image Types

### JPEG
```bash
curl -X POST http://localhost:8087/api/media/upload \
  -H "Authorization: Bearer TOKEN" \
  -F "file=@photo.jpg"
```

### PNG
```bash
curl -X POST http://localhost:8087/api/media/upload \
  -H "Authorization: Bearer TOKEN" \
  -F "file=@screenshot.png"
```

### WebP
```bash
curl -X POST http://localhost:8087/api/media/upload \
  -H "Authorization: Bearer TOKEN" \
  -F "file=@image.webp"
```

### GIF
```bash
curl -X POST http://localhost:8087/api/media/upload \
  -H "Authorization: Bearer TOKEN" \
  -F "file=@animation.gif"
```

---

## Rate Limiting (Future Enhancement)

Consider adding rate limiting:
- Max 10 uploads per minute per user
- Max 100 uploads per day per user
- Max 50MB total upload per day per user

---

## Monitoring

### Check Service Status
```bash
curl http://localhost:8087/actuator/health
```

### View Metrics
```bash
curl http://localhost:8087/actuator/metrics
```

### View Info
```bash
curl http://localhost:8087/actuator/info
```

---

## Swagger UI

Access interactive API documentation:
```
http://localhost:8087/swagger-ui.html
```

Or OpenAPI JSON:
```
http://localhost:8087/v3/api-docs
```
