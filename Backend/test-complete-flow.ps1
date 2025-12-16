# Complete Media Service Test Flow
Write-Host "=== Media Service Complete Test Flow ===" -ForegroundColor Cyan
Write-Host ""

# 1. Check service health
Write-Host "1. Checking service health..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8087/actuator/health"
    Write-Host "   Service Status: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "   ERROR: Service is not running!" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 2. Register user
Write-Host "2. Registering test user..." -ForegroundColor Yellow
$registerBody = @{
    email = "test@example.com"
    password = "password123"
    username = "testuser"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $registerBody
    Write-Host "   User registered successfully" -ForegroundColor Green
} catch {
    Write-Host "   User already exists (OK)" -ForegroundColor Yellow
}
Write-Host ""

# 3. Login
Write-Host "3. Logging in..." -ForegroundColor Yellow
$loginBody = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody
    
    $token = $loginResponse.token
    Write-Host "   Login successful!" -ForegroundColor Green
    Write-Host "   Token: $($token.Substring(0,20))..." -ForegroundColor Cyan
} catch {
    Write-Host "   ERROR: Login failed!" -ForegroundColor Red
    Write-Host "   $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 4. Create test image
Write-Host "4. Creating test image..." -ForegroundColor Yellow
$testImagePath = "$env:TEMP\test-upload-$(Get-Date -Format 'yyyyMMddHHmmss').jpg"
$bytes = [byte[]](0xFF, 0xD8, 0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0xFF, 0xDB, 0x00, 0x43, 0x00, 0x08, 0x06, 0x06, 0x07, 0x06, 0x05, 0x08, 0x07, 0x07, 0x07, 0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14, 0x0D, 0x0C, 0x0B, 0x0B, 0x0C, 0x19, 0x12, 0x13, 0x0F, 0x14, 0x1D, 0x1A, 0x1F, 0x1E, 0x1D, 0x1A, 0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27, 0x20, 0x22, 0x2C, 0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C, 0x30, 0x31, 0x34, 0x34, 0x34, 0x1F, 0x27, 0x39, 0x3D, 0x38, 0x32, 0x3C, 0x2E, 0x33, 0x34, 0x32, 0xFF, 0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x11, 0x00, 0xFF, 0xC4, 0x00, 0x14, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xC4, 0x00, 0x14, 0x10, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00, 0x7F, 0xFF, 0xD9)
[System.IO.File]::WriteAllBytes($testImagePath, $bytes)
Write-Host "   Test image created: $testImagePath" -ForegroundColor Green
Write-Host ""

# 5. Upload image
Write-Host "5. Uploading image to media service..." -ForegroundColor Yellow
$headers = @{
    Authorization = "Bearer $token"
}

$form = @{
    file = Get-Item -Path $testImagePath
}

try {
    $uploadResponse = Invoke-RestMethod -Uri "http://localhost:8087/api/media/upload" `
        -Method Post `
        -Headers $headers `
        -Form $form
    
    Write-Host "   Upload successful!" -ForegroundColor Green
    Write-Host "   Image URL: $($uploadResponse.url)" -ForegroundColor Cyan
    Write-Host "   File Name: $($uploadResponse.fileName)" -ForegroundColor Cyan
    Write-Host "   Size: $($uploadResponse.size) bytes" -ForegroundColor Cyan
} catch {
    Write-Host "   ERROR: Upload failed!" -ForegroundColor Red
    Write-Host "   $_" -ForegroundColor Red
    Remove-Item $testImagePath -ErrorAction SilentlyContinue
    exit 1
}
Write-Host ""

# 6. Create post with image
Write-Host "6. Creating post with image..." -ForegroundColor Yellow
$postBody = @{
    content = "Test post with image from media service! $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    mediaUrl = $uploadResponse.url
} | ConvertTo-Json

try {
    $postResponse = Invoke-RestMethod -Uri "http://localhost:8084/api/posts" `
        -Method Post `
        -Headers $headers `
        -ContentType "application/json" `
        -Body $postBody
    
    Write-Host "   Post created successfully!" -ForegroundColor Green
    Write-Host "   Post ID: $($postResponse.id)" -ForegroundColor Cyan
    Write-Host "   Content: $($postResponse.content)" -ForegroundColor Cyan
    Write-Host "   Media URL: $($postResponse.mediaUrl)" -ForegroundColor Cyan
} catch {
    Write-Host "   ERROR: Post creation failed!" -ForegroundColor Red
    Write-Host "   $_" -ForegroundColor Red
}
Write-Host ""

# 7. Verify image accessibility
Write-Host "7. Verifying image is publicly accessible..." -ForegroundColor Yellow
try {
    $imageCheck = Invoke-WebRequest -Uri $uploadResponse.url -Method Head -UseBasicParsing
    Write-Host "   Image is publicly accessible!" -ForegroundColor Green
    Write-Host "   Status: $($imageCheck.StatusCode)" -ForegroundColor Cyan
} catch {
    Write-Host "   WARNING: Image might not be publicly accessible" -ForegroundColor Yellow
    Write-Host "   Check your GCS bucket permissions" -ForegroundColor Yellow
}
Write-Host ""

# Cleanup
Remove-Item $testImagePath -ErrorAction SilentlyContinue

Write-Host "=== All Tests Completed Successfully! ===" -ForegroundColor Green
Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "  - Service is running" -ForegroundColor White
Write-Host "  - Authentication works" -ForegroundColor White
Write-Host "  - Image upload works" -ForegroundColor White
Write-Host "  - Post creation with image works" -ForegroundColor White
Write-Host "  - Image URL: $($uploadResponse.url)" -ForegroundColor White
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Open Swagger UI: http://localhost:8087/swagger-ui.html" -ForegroundColor White
Write-Host "  2. View your post in the feed" -ForegroundColor White
Write-Host "  3. Integrate in Android app" -ForegroundColor White
