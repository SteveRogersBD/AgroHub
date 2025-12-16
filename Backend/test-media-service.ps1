# Media Service Test Script
# This script helps you test the media-service endpoints

Write-Host "=== Media Service Test Script ===" -ForegroundColor Cyan
Write-Host ""

# 1. Health Check
Write-Host "1. Testing Health Check..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8087/actuator/health" -Method Get
    Write-Host "Service is UP!" -ForegroundColor Green
    Write-Host "Status: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "Service is DOWN!" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== Service is Ready! ===" -ForegroundColor Green
Write-Host ""

# Instructions
Write-Host "To test upload, you need a JWT token" -ForegroundColor Yellow
Write-Host ""
Write-Host "Step 1: Login to get token" -ForegroundColor Cyan
Write-Host '  $body = @{ email = "test@example.com"; password = "password123" } | ConvertTo-Json'
Write-Host '  $login = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -ContentType "application/json" -Body $body'
Write-Host '  $token = $login.token'
Write-Host ""
Write-Host "Step 2: Upload Image" -ForegroundColor Cyan
Write-Host '  $headers = @{ Authorization = "Bearer $token" }'
Write-Host '  $form = @{ file = Get-Item "C:\path\to\image.jpg" }'
Write-Host '  $upload = Invoke-RestMethod -Uri "http://localhost:8087/api/media/upload" -Method Post -Headers $headers -Form $form'
Write-Host '  Write-Host "Image URL: $($upload.url)"'
Write-Host ""
Write-Host "Swagger UI: http://localhost:8087/swagger-ui.html" -ForegroundColor Cyan
Write-Host "View Logs: docker logs media-service" -ForegroundColor Cyan
