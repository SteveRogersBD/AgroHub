# Service Health Diagnostic Script

Write-Host "=== AgroHub Backend Services Diagnostic ===" -ForegroundColor Cyan
Write-Host ""

# Check API Gateway
Write-Host "Checking API Gateway (port 8080)..." -ForegroundColor Yellow
try {
    $response = curl.exe -s http://localhost:8080/actuator/health
    Write-Host "✓ API Gateway: $response" -ForegroundColor Green
} catch {
    Write-Host "✗ API Gateway: FAILED" -ForegroundColor Red
}

Write-Host ""

# Check individual services through their direct ports
$services = @(
    @{Name="Auth Service"; Port=8081},
    @{Name="User Service"; Port=8082},
    @{Name="Follow Service"; Port=8083},
    @{Name="Post Service"; Port=8084},
    @{Name="Comment Service"; Port=8085},
    @{Name="Like Service"; Port=8086},
    @{Name="Media Service"; Port=8087},
    @{Name="Feed Service"; Port=8089},
    @{Name="Notification Service"; Port=8090}
)

foreach ($service in $services) {
    Write-Host "Checking $($service.Name) (port $($service.Port))..." -ForegroundColor Yellow
    $response = curl.exe -s "http://localhost:$($service.Port)/actuator/health" 2>$null
    if ($response -match "UP") {
        Write-Host "✓ $($service.Name): UP" -ForegroundColor Green
    } elseif ($response) {
        Write-Host "✗ $($service.Name): $response" -ForegroundColor Red
    } else {
        Write-Host "✗ $($service.Name): Cannot connect" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== Testing API Gateway Routes ===" -ForegroundColor Cyan
Write-Host ""

# Test if gateway can route to services
Write-Host "Testing gateway routing (should get 401 Unauthorized for protected endpoints)..." -ForegroundColor Yellow

$endpoints = @(
    @{Name="Auth - Register"; Url="http://localhost:8080/api/auth/register"; Method="POST"},
    @{Name="Posts"; Url="http://localhost:8080/api/posts"; Method="GET"},
    @{Name="Feed"; Url="http://localhost:8080/api/feed"; Method="GET"}
)

foreach ($endpoint in $endpoints) {
    Write-Host "Testing $($endpoint.Name)..." -ForegroundColor Yellow
    $statusCode = curl.exe -s -o nul -w "%{http_code}" "$($endpoint.Url)" 2>$null
    if ($statusCode -eq "401" -or $statusCode -eq "403") {
        Write-Host "✓ $($endpoint.Name): Gateway routing works (got $statusCode as expected)" -ForegroundColor Green
    } elseif ($statusCode -eq "200") {
        Write-Host "✓ $($endpoint.Name): Accessible (got $statusCode)" -ForegroundColor Green
    } elseif ($statusCode) {
        Write-Host "⚠ $($endpoint.Name): Got status $statusCode" -ForegroundColor Yellow
    } else {
        Write-Host "✗ $($endpoint.Name): Failed to connect" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== Docker Container Status ===" -ForegroundColor Cyan
Write-Host ""
docker-compose ps --format "table {{.Service}}\t{{.Status}}"

Write-Host ""
Write-Host "=== Diagnosis Complete ===" -ForegroundColor Cyan
