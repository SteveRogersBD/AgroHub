# Test Community APIs

Write-Host "=== Testing Community APIs ===" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api"

# Step 1: Register a test user
Write-Host "1. Registering test user..." -ForegroundColor Yellow
$registerData = @{
    email = "testuser@example.com"
    username = "testuser"
    password = "Test123456!"
} | ConvertTo-Json

$registerResponse = curl.exe -s -X POST "$baseUrl/auth/register" `
    -H "Content-Type: application/json" `
    -d $registerData 2>$null

if ($registerResponse -match "id") {
    Write-Host "   User registered successfully" -ForegroundColor Green
} else {
    Write-Host "   User may already exist (this is OK)" -ForegroundColor Yellow
}

# Step 2: Login
Write-Host "2. Logging in..." -ForegroundColor Yellow
$loginData = @{
    email = "testuser@example.com"
    password = "Test123456!"
} | ConvertTo-Json

$loginResponse = curl.exe -s -X POST "$baseUrl/auth/login" `
    -H "Content-Type: application/json" `
    -d $loginData

$loginJson = $loginResponse | ConvertFrom-Json
$token = $loginJson.accessToken

if ($token) {
    Write-Host "   Login successful! Token received." -ForegroundColor Green
} else {
    Write-Host "   Login failed!" -ForegroundColor Red
    Write-Host "   Response: $loginResponse"
    exit 1
}

# Step 3: Test Feed API
Write-Host "3. Testing Feed API..." -ForegroundColor Yellow
$feedResponse = curl.exe -s -X GET "$baseUrl/feed?page=0&size=10" `
    -H "Authorization: Bearer $token"

if ($feedResponse -match "content" -or $feedResponse -match "\[\]") {
    Write-Host "   Feed API works! (may be empty if no posts)" -ForegroundColor Green
} else {
    Write-Host "   Feed API response: $feedResponse" -ForegroundColor Yellow
}

# Step 4: Test Create Post API
Write-Host "4. Testing Create Post API..." -ForegroundColor Yellow
$postData = @{
    content = "Test post from PowerShell script - $(Get-Date)"
    mediaUrl = $null
} | ConvertTo-Json

$createPostResponse = curl.exe -s -X POST "$baseUrl/posts" `
    -H "Authorization: Bearer $token" `
    -H "Content-Type: application/json" `
    -d $postData

if ($createPostResponse -match "id") {
    Write-Host "   Post created successfully!" -ForegroundColor Green
    $postJson = $createPostResponse | ConvertFrom-Json
    $postId = $postJson.id
    Write-Host "   Post ID: $postId"
} else {
    Write-Host "   Create post response: $createPostResponse" -ForegroundColor Yellow
}

# Step 5: Test Like API
if ($postId) {
    Write-Host "5. Testing Like API..." -ForegroundColor Yellow
    $likeResponse = curl.exe -s -X POST "$baseUrl/likes/$postId" `
        -H "Authorization: Bearer $token"
    
    if ($likeResponse -eq "" -or $likeResponse -match "success") {
        Write-Host "   Like API works!" -ForegroundColor Green
    } else {
        Write-Host "   Like response: $likeResponse" -ForegroundColor Yellow
    }
}

# Step 6: Test Comment API
if ($postId) {
    Write-Host "6. Testing Comment API..." -ForegroundColor Yellow
    $commentData = @{
        postId = $postId
        content = "Test comment from PowerShell"
    } | ConvertTo-Json
    
    $commentResponse = curl.exe -s -X POST "$baseUrl/comments" `
        -H "Authorization: Bearer $token" `
        -H "Content-Type: application/json" `
        -d $commentData
    
    if ($commentResponse -match "id") {
        Write-Host "   Comment created successfully!" -ForegroundColor Green
    } else {
        Write-Host "   Comment response: $commentResponse" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "- If all tests passed, your backend is working correctly!" -ForegroundColor Green
Write-Host "- The Android app should now be able to:" -ForegroundColor Green
Write-Host "  * View the feed" -ForegroundColor Green
Write-Host "  * Create posts" -ForegroundColor Green
Write-Host "  * Like posts" -ForegroundColor Green
Write-Host "  * Add comments" -ForegroundColor Green
