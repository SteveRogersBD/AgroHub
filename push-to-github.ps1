# GitHub Push Script for AgroHub
# This script stages all changes, commits, and pushes to GitHub

param(
    [string]$commitMessage = "Update project files"
)

Write-Host "=== AgroHub GitHub Push Script ===" -ForegroundColor Cyan
Write-Host ""

# Check if we're in a git repository
if (-not (Test-Path ".git")) {
    Write-Host "Error: Not a git repository!" -ForegroundColor Red
    exit 1
}

# Show current status
Write-Host "Current Git Status:" -ForegroundColor Yellow
git status --short

Write-Host ""
Write-Host "Staging all changes..." -ForegroundColor Yellow
git add .

# Show what will be committed
Write-Host ""
Write-Host "Files to be committed:" -ForegroundColor Yellow
git status --short

# Commit with message
Write-Host ""
Write-Host "Committing changes..." -ForegroundColor Yellow
git commit -m "$commitMessage"

if ($LASTEXITCODE -ne 0) {
    Write-Host "No changes to commit or commit failed!" -ForegroundColor Red
    exit 1
}

# Get current branch
$branch = git rev-parse --abbrev-ref HEAD

# Push to GitHub
Write-Host ""
Write-Host "Pushing to GitHub (branch: $branch)..." -ForegroundColor Yellow
git push origin $branch

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ Successfully pushed to GitHub!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "✗ Push failed! Check your credentials and network connection." -ForegroundColor Red
    exit 1
}
