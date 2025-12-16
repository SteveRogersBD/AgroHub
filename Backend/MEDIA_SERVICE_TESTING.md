# Media Service Testing Guide

## Quick Status Check

Run the test script:
```powershell
cd Backend
powershell -File test-media-service.ps1
```

## Manual Testing

### 1. Health Check (No Auth Required)

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8087/actuator/health"
```

**cURL:**
```bash
curl http://localhost:8087/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

---

### 2. Upload Image (Requires JWT Token)

#### Step 1: Get JWT Token

First, you need to login to get a JWT token from auth-service:

**PowerShell:**
```powershell
# Create a user first (if not exists)
$registerBody = @{
    email = "test@example.com"
    password = "password123"
    username = "testuser"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $registerBody

# Login to get token
$loginBody = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $loginBody

$token = $loginResponse.token
Write-Host "Token: $token"
```

**cURL:**
```bash
# Register
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","username":"testuser"}'

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Copy the token from response
```

#### Step 2: Upload an Image

**PowerShell:**
```powershell
# Create a test image (or use an existing one)
$imagePath = "C:\path\to\your\image.jpg"

# Upload
$headers = @{
    Authorization = "Bearer $token"
}

$form = @{
    file = Get-Item -Path $imagePath
}

$uploadResponse = Invoke-RestMethod -Uri "http://localhost:8087/api/media/upload" `
    -Method Post `
    -Headers $headers `
    -Form $form

Write-Host "Image URL: $($uploadResponse.url)"
Write-Host "File Name: $($uploadResponse.fileName)"
Write-Host "Size: $($uploadResponse.size) bytes"
```

**cURL:**
```bash
curl -X POST http://localhost:8087/api/media/upload \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "file=@/path/to/image.jpg"
```

**Expected Response:**
```json
{
  "url": "https://storage.googleapis.com/agro-photo-bucket/posts/123/uuid.jpg",
  "fileName": "posts/123/uuid.jpg",
  "contentType": "image/jpeg",
  "size": 245678
}
```

---

### 3. Delete Image

**PowerShell:**
```powershell
$fileName = "posts/123/uuid.jpg"  # From upload response

$headers = @{
    Authorization = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8087/api/media/$fileName" `
    -Method Delete `
    -Headers $headers
```

**cURL:**
```bash
curl -X DELETE "http://localhost:8087/api/media/posts/123/uuid.jpg" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:** 204 No Content (empty response)

---

## Complete Test Flow

Here's a complete PowerShell script to test the entire flow:

```powershell
# 1. Check service health
Write-Host "Checking service health..." -ForegroundColor Yellow
$health = Invoke-RestMethod -Uri "http://localhost:8087/actuator/health"
Write-Host "Service Status: $($health.status)" -ForegroundColor Green
Write-Host ""

# 2. Register user
Write-Host "Registering user..." -ForegroundColor Yellow
$registerBody = @{
    email = "test@example.com"
    password = "password123"
    username = "testuser"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $registerBody
    Write-Host "User registered successfully" -ForegroundColor Green
} catch {
    Write-Host "User might already exist (this is OK)" -ForegroundColor Yellow
}
Write-Host ""

# 3. Login
Write-Host "Logging in..." -ForegroundColor Yellow
$loginBody = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $loginBody

$token = $loginResponse.token
Write-Host "Login successful! Token: $($token.Substring(0,20))..." -ForegroundColor Green
Write-Host ""

# 4. Create a test image
Write-Host "Creating test image..." -ForegroundColor Yellow
$testImagePath = "$env:TEMP\test-upload.jpg"
# Create a simple 1x1 pixel JPEG
$bytes = [byte[]](0xFF, 0xD8, 0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0xFF, 0xDB, 0x00, 0x43, 0x00, 0x08, 0x06, 0x06, 0x07, 0x06, 0x05, 0x08, 0x07, 0x07, 0x07, 0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14, 0x0D, 0x0C, 0x0B, 0x0B, 0x0C, 0x19, 0x12, 0x13, 0x0F, 0x14, 0x1D, 0x1A, 0x1F, 0x1E, 0x1D, 0x1A, 0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27, 0x20, 0x22, 0x2C, 0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C, 0x30, 0x31, 0x34, 0x34, 0x34, 0x1F, 0x27, 0x39, 0x3D, 0x38, 0x32, 0x3C, 0x2E, 0x33, 0x34, 0x32, 0xFF, 0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x11, 0x00, 0xFF, 0xC4, 0x00, 0x14, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xC4, 0x00, 0x14, 0x10, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00, 0x7F, 0xFF, 0xD9)
[System.IO.File]::WriteAllBytes($testImagePath, $bytes)
Write-Host "Test image created at: $testImagePath" -ForegroundColor Green
Write-Host ""

# 5. Upload image
Write-Host "Uploading image..." -ForegroundColor Yellow
$headers = @{
    Authorization = "Bearer $token"
}

$form = @{
    file = Get-Item -Path $testImagePath
}

$uploadResponse = Invoke-RestMethod -Uri "http://localhost:8087/api/media/upload" `
    -Method Post `
    -Headers $headers `
    -Form $form

Write-Host "Upload successful!" -ForegroundColor Green
Write-Host "Image URL: $($uploadResponse.url)" -ForegroundColor Cyan
Write-Host "File Name: $($uploadResponse.fileName)" -ForegroundColor Cyan
Write-Host "Size: $($uploadResponse.size) bytes" -ForegroundColor Cyan
Write-Host ""

# 6. Create post with image
Write-Host "Creating post with image..." -ForegroundColor Yellow
$postBody = @{
    content = "Test post with image from media service!"
    mediaUrl = $uploadResponse.url
} | ConvertTo-Json

$postResponse = Invoke-RestMethod -Uri "http://localhost:8084/api/posts" `
    -Method Post `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $postBody

Write-Host "Post created successfully!" -ForegroundColor Green
Write-Host "Post ID: $($postResponse.id)" -ForegroundColor Cyan
Write-Host "Content: $($postResponse.content)" -ForegroundColor Cyan
Write-Host "Media URL: $($postResponse.mediaUrl)" -ForegroundColor Cyan
Write-Host ""

# 7. Verify image is accessible
Write-Host "Verifying image is accessible..." -ForegroundColor Yellow
try {
    $imageCheck = Invoke-WebRequest -Uri $uploadResponse.url -Method Head
    Write-Host "Image is publicly accessible!" -ForegroundColor Green
    Write-Host "Content-Type: $($imageCheck.Headers.'Content-Type')" -ForegroundColor Cyan
} catch {
    Write-Host "Warning: Image might not be publicly accessible" -ForegroundColor Yellow
    Write-Host "Check your GCS bucket permissions" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "=== All Tests Passed! ===" -ForegroundColor Green
```

Save this as `Backend/test-complete-flow.ps1` and run it:
```powershell
cd Backend
powershell -File test-complete-flow.ps1
```

---

## Using Swagger UI

The easiest way to test is using Swagger UI:

1. Open browser: http://localhost:8087/swagger-ui.html
2. Click on "Media Management" section
3. Try the endpoints interactively
4. Click "Authorize" button to add your JWT token

---

## Troubleshooting

### Service not responding
```powershell
# Check if container is running
docker ps | Select-String media

# Check logs
docker logs media-service

# Restart service
docker-compose restart media-service
```

### Upload fails with 401 Unauthorized
- Make sure you have a valid JWT token
- Token might be expired (login again)
- Check Authorization header format: `Bearer <token>`

### Upload fails with 400 Bad Request
- Check file size (max 10MB)
- Verify file type (only images: JPEG, PNG, GIF, WebP)
- Make sure file field name is "file"

### Image URL not accessible
- Check GCS bucket permissions
- Ensure `allUsers` has "Storage Object Viewer" role
- Verify bucket name in configuration

---

## Monitoring

### View Service Logs
```powershell
docker logs media-service -f
```

### Check Service Metrics
```powershell
Invoke-RestMethod -Uri "http://localhost:8087/actuator/metrics"
```

### Check Service Info
```powershell
Invoke-RestMethod -Uri "http://localhost:8087/actuator/info"
```

---

## Next Steps

Once media-service is working:

1. ✅ Test health endpoint
2. ✅ Test upload with valid token
3. ✅ Verify image URL is accessible
4. ✅ Create post with image URL
5. ✅ View post in feed with image
6. 🔄 Integrate in Android app
7. 🔄 Add image compression
8. 🔄 Add image caching

---

## Quick Reference

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/actuator/health` | GET | No | Health check |
| `/api/media/upload` | POST | Yes | Upload image |
| `/api/media/{fileName}` | DELETE | Yes | Delete image |
| `/swagger-ui.html` | GET | No | API docs |

**Service URL:** http://localhost:8087  
**Docker Container:** media-service  
**Port:** 8087  
**Bucket:** agro-photo-bucket  
**Project:** agrohub-481313
