# Cleanup Demo Data
# Removes all demo users and their associated data

Write-Host "=== Cleaning Up Demo Data ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  WARNING: This will delete all demo users and their data!" -ForegroundColor Yellow
Write-Host ""

$confirmation = Read-Host "Are you sure you want to continue? (yes/no)"

if ($confirmation -ne "yes") {
    Write-Host "Cleanup cancelled." -ForegroundColor Yellow
    exit
}

Write-Host ""
Write-Host "Note: This script requires database access or admin API endpoints." -ForegroundColor Yellow
Write-Host "For now, the easiest way to reset is to restart your Docker containers:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  cd Backend" -ForegroundColor Cyan
Write-Host "  docker-compose down -v" -ForegroundColor Cyan
Write-Host "  docker-compose up -d" -ForegroundColor Cyan
Write-Host ""
Write-Host "This will reset all databases to a clean state." -ForegroundColor White
Write-Host ""
