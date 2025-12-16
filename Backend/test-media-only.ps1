# Simple Media Service Health Test
Write-Host "=== Media Service Health Check ===" -ForegroundColor Cyan
Write-Host ""

# Check if service is running
Write-Host "Checking if media-service is running..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8087/actuator/health" -UseBasicParsing
    Write-Host "SUCCESS: Media service is UP!" -ForegroundColor Green
    Write-Host "Status: $($health.status)" -ForegroundColor Cyan
    Write-Host ""
    
    Write-Host "Service Details:" -ForegroundColor Yellow
    Write-Host "  URL: http://localhost:8087" -ForegroundColor White
    Write-Host "  Swagger UI: http://localhost:8087/swagger-ui.html" -ForegroundColor White
    Write-Host "  Container: media-service" -ForegroundColor White
    Write-Host "  Port: 8087" -ForegroundColor White
    Write-Host ""
    
    Write-Host "Available Endpoints:" -ForegroundColor Yellow
    Write-Host "  GET  /actuator/health          - Health check (no auth)" -ForegroundColor White
    Write-Host "  POST /api/media/upload         - Upload image (requires JWT)" -ForegroundColor White
    Write-Host "  DELETE /api/media/{fileName}   - Delete image (requires JWT)" -ForegroundColor White
    Write-Host ""
    
    Write-Host "To test upload:" -ForegroundColor Yellow
    Write-Host "  1. Get JWT token from auth-service (login)" -ForegroundColor White
    Write-Host "  2. Use the token to upload an image" -ForegroundColor White
    Write-Host "  3. See MEDIA_SERVICE_TESTING.md for examples" -ForegroundColor White
    Write-Host ""
    
    Write-Host "View logs:" -ForegroundColor Yellow
    Write-Host "  docker logs media-service" -ForegroundColor White
    Write-Host ""
    
} catch {
    Write-Host "ERROR: Media service is not responding!" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "  1. Check if container is running: docker ps | Select-String media" -ForegroundColor White
    Write-Host "  2. Check logs: docker logs media-service" -ForegroundColor White
    Write-Host "  3. Restart service: docker-compose restart media-service" -ForegroundColor White
    exit 1
}

Write-Host "=== Media Service is Ready! ===" -ForegroundColor Green
