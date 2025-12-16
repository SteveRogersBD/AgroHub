# Media Service - Quick Start

## ✅ Service Status

Your media-service is **RUNNING** and ready to use!

- **URL**: http://localhost:8087
- **Status**: UP ✓
- **Container**: media-service
- **Port**: 8087

## 🚀 Quick Test

Run this to verify:
```powershell
cd Backend
powershell -File test-media-only.ps1
```

## 📋 Available Endpoints

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/actuator/health` | GET | ❌ No | Check service health |
| `/api/media/upload` | POST | ✅ Yes | Upload image file |
| `/api/media/{fileName}` | DELETE | ✅ Yes | Delete image |
| `/swagger-ui.html` | GET | ❌ No | Interactive API docs |

## 🔑 How to Test Upload

### 1. Get a JWT Token

Login to auth-service to get a token:

```powershell
$body = @{
    email = "your-email@example.com"
    password = "your-password"
} | ConvertTo-Json

$login = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body

$token = $login.token
Write-Host "Token: $token"
```

### 2. Upload an Image

```powershell
$headers = @{ Authorization = "Bearer $token" }
$form = @{ file = Get-Item "C:\path\to\your\image.jpg" }

$upload = Invoke-RestMethod -Uri "http://localhost:8087/api/media/upload" `
    -Method Post `
    -Headers $headers `
    -Form $form

Write-Host "Image URL: $($upload.url)"
```

### 3. Use the URL in a Post

```powershell
$postBody = @{
    content = "Check out this photo!"
    mediaUrl = $upload.url
} | ConvertTo-Json

$post = Invoke-RestMethod -Uri "http://localhost:8084/api/posts" `
    -Method Post `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $postBody

Write-Host "Post created with image!"
```

## 🌐 Interactive Testing

Open Swagger UI in your browser for easy testing:

**http://localhost:8087/swagger-ui.html**

1. Click "Authorize" button
2. Enter: `Bearer YOUR_JWT_TOKEN`
3. Try the upload endpoint with a file

## 📊 Monitoring

### View Logs
```powershell
docker logs media-service -f
```

### Check Health
```powershell
Invoke-RestMethod http://localhost:8087/actuator/health
```

### Restart Service
```powershell
docker-compose restart media-service
```

## 📖 Documentation

- **Setup Guide**: `MEDIA_SERVICE_SETUP.md`
- **Testing Guide**: `MEDIA_SERVICE_TESTING.md`
- **Android Integration**: `ANDROID_IMAGE_UPLOAD_GUIDE.md`
- **API Examples**: `media-service/API_EXAMPLES.md`
- **Summary**: `MEDIA_SERVICE_SUMMARY.md`

## 🎯 Next Steps

### Backend (Complete ✅)
- [x] Media service created
- [x] Google Cloud Storage configured
- [x] Service running in Docker
- [x] Endpoints working
- [x] Post entity has mediaUrl field

### Android (To Do 📱)
1. Add MediaApi interface to your app
2. Create MediaRepository
3. Update CreatePostScreen with image picker
4. Upload image before creating post
5. Display images in feed using Coil

See `ANDROID_IMAGE_UPLOAD_GUIDE.md` for complete Android implementation.

## ⚙️ Configuration

Your current setup:
- **Project ID**: agrohub-481313
- **Bucket**: agro-photo-bucket
- **Credentials**: ./Backend/agro-service.json
- **Max File Size**: 10MB
- **Allowed Types**: JPEG, PNG, GIF, WebP

## 🔧 Troubleshooting

### Service not responding?
```powershell
docker ps | Select-String media
docker logs media-service
docker-compose restart media-service
```

### Upload fails?
- Check JWT token is valid
- Verify file size < 10MB
- Ensure file is an image type
- Check Authorization header format

### Image not accessible?
- Verify GCS bucket has public access
- Check bucket permissions in Google Cloud Console

## 💡 Tips

1. **Use Swagger UI** for quick testing
2. **Check logs** if something fails
3. **Test with small images** first
4. **Compress images** before upload in production
5. **Cache images** in Android app

## 🎉 You're Ready!

The media service is fully functional and ready to handle image uploads. Start integrating it into your Android app!

**Questions?** Check the documentation files or run the test scripts.
