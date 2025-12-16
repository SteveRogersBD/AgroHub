# Simple Service Health Check

Write-Host "=== Checking Backend Services ===" -ForegroundColor Cyan
Write-Host ""

# Check API Gateway
Write-Host "API Gateway (8080):" -NoNewline
$gw = curl.exe -s http://localhost:8080/actuator/health 2>$null
if ($gw -match "UP") { Write-Host " ✓ UP" -ForegroundColor Green } else { Write-Host " ✗ DOWN" -ForegroundColor Red }

# Check Auth Service
Write-Host "Auth Service (8081):" -NoNewline
$auth = curl.exe -s http://localhost:8081/actuator/health 2>$null
if ($auth -match "UP") { Write-Host " ✓ UP" -ForegroundColor Green } else { Write-Host " ✗ DOWN/PROTECTED" -ForegroundColor Yellow }

# Check User Service
Write-Host "User Service (8082):" -NoNewline
$user = curl.exe -s http://localhost:8082/actuator/health 2>$null
if ($user -match "UP") { Write-Host " ✓ UP" -ForegroundColor Green } else { Write-Host " ✗ DOWN/PROTECTED" -ForegroundColor Yellow }

# Check Follow Service
Write-Host "Follow Service (8083):" -NoNewline
$follow = curl.exe -s http://localhost:8083/actuator/health 2>$null
if ($follow -match "UP") { Write-Host " ✓ UP" -ForegroundColor Green } else { Write-Host " ✗ DOWN/PROTECTED" -ForegroundColor Yellow }

# Check Post Service
Write-Host "Post Service (8084):" -NoNewline
$post = curl.exe -s http://localhost:8084/actuator/health 2>$null
if ($post -match "UP") { Write-Host " ✓ UP" -ForegroundColor Green } else { Write-Host " ✗ DOWN/PROTECTED" -ForegroundColor Yellow }

# Check Comment Service
Write-Host "Comment Service (8085):" -NoNewline
$comment = curl.exe -s http://localhost:8085/actuator/health 2>$null
if ($comment -match "UP") { Write-Host " ✓ UP" -ForegroundColor Green } else { Write-Host " ✗ DOWN/PROTECTED" -ForegroundColor Yellow }

# Check Like Service
Write-Host "Like Service (8086):" -NoNewline
$like = curl.exe -s http://localhost:8086/actuator/health 2>$null
if ($like -match "UP") { Write-Host " ✓ UP" -ForegroundColor Green } else { Write-Host " ✗ DOWN/PROTECTED" -ForegroundColor Yellow }

# Check Media Service
Write-Host "Media Service (8087):" -NoNewline
$media = curl.exe -s http://localhost:8087/actuator/health 2>$null
if ($media -match "UP") { Write-Host " ✓ UP" -ForegroundColor Green } else { Write-Host " ✗ DOWN/PROTECTED" -ForegroundColor Yellow }

# Check Feed Service
Write-Host "Feed Service (8089):" -NoNewline
$feed = curl.exe -s http://localhost:8089/actuator/health 2>$null
if ($feed -match "UP") { Write-Host " ✓ UP" -ForegroundColor Green } else { Write-Host " ✗ DOWN/PROTECTED" -ForegroundColor Yellow }

# Check Notification Service
Write-Host "Notification Service (8090):" -NoNewline
$notif = curl.exe -s http://localhost:8090/actuator/health 2>$null
if ($notif -match "UP") { Write-Host " ✓ UP" -ForegroundColor Green } else { Write-Host " ✗ DOWN/PROTECTED" -ForegroundColor Yellow }

Write-Host ""
Write-Host "=== Testing Gateway Routes ===" -ForegroundColor Cyan
Write-Host ""

# Test Feed endpoint (should return 401 without auth)
Write-Host "GET /api/feed:" -NoNewline
$feedStatus = curl.exe -s -o nul -w "%{http_code}" http://localhost:8080/api/feed 2>$null
if ($feedStatus -eq "401") {
    Write-Host " ✓ Gateway routing works (401 Unauthorized)" -ForegroundColor Green
} else {
    Write-Host " Status: $feedStatus" -ForegroundColor Yellow
}

# Test Posts endpoint (should return 401 without auth)
Write-Host "GET /api/posts:" -NoNewline
$postsStatus = curl.exe -s -o nul -w "%{http_code}" http://localhost:8080/api/posts 2>$null
if ($postsStatus -eq "401") {
    Write-Host " ✓ Gateway routing works (401 Unauthorized)" -ForegroundColor Green
} else {
    Write-Host " Status: $postsStatus" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Note: Services showing DOWN/PROTECTED may have secured health endpoints." -ForegroundColor Cyan
Write-Host "If Gateway is UP and routing works, the services are likely functioning." -ForegroundColor Cyan
