# Check core services status
Write-Host "Checking Docker Containers Status..."
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Check specific ports
Write-Host "`nChecking Ports..."
$ports = @(8080, 8081, 5432)
foreach ($port in $ports) {
    if (Test-NetConnection -ComputerName localhost -Port $port -InformationLevel Quiet) {
        Write-Host "Port $port is listening" -ForegroundColor Green
    } else {
        Write-Host "Port $port is NOT listening" -ForegroundColor Red
    }
}

# Check Gateway -> Auth connectivity
Write-Host "`nChecking Gateway -> Auth Service..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" -Method POST -Body "{}" -ContentType "application/json" -ErrorAction Stop
} catch {
    Write-Host "Response: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)"
    }
}

# Check Auth Service logs for errors
Write-Host "`nChecking Auth Service Logs (Last 50 lines)..."
docker logs auth-service --tail 50
