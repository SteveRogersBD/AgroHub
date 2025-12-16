@echo off
REM GitHub Push Script for AgroHub (CMD version)
REM This script stages all changes, commits, and pushes to GitHub

setlocal

set "COMMIT_MESSAGE=%~1"
if "%COMMIT_MESSAGE%"=="" set "COMMIT_MESSAGE=Update project files"

echo === AgroHub GitHub Push Script ===
echo.

REM Check if we're in a git repository
if not exist ".git" (
    echo Error: Not a git repository!
    exit /b 1
)

REM Show current status
echo Current Git Status:
git status --short

echo.
echo Staging all changes...
git add .

REM Show what will be committed
echo.
echo Files to be committed:
git status --short

REM Commit with message
echo.
echo Committing changes...
git commit -m "%COMMIT_MESSAGE%"

if errorlevel 1 (
    echo No changes to commit or commit failed!
    exit /b 1
)

REM Get current branch
for /f "tokens=*" %%i in ('git rev-parse --abbrev-ref HEAD') do set BRANCH=%%i

REM Push to GitHub
echo.
echo Pushing to GitHub (branch: %BRANCH%)...
git push origin %BRANCH%

if errorlevel 1 (
    echo.
    echo Push failed! Check your credentials and network connection.
    exit /b 1
) else (
    echo.
    echo Successfully pushed to GitHub!
)

endlocal
