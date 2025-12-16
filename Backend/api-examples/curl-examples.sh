#!/bin/bash

# Social Media Platform API - cURL Examples
# This script demonstrates how to use the API with cURL commands

# Configuration
BASE_URL="http://localhost:8080/api"
ACCESS_TOKEN=""
REFRESH_TOKEN=""
USER_ID=""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Helper function to print section headers
print_section() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

# Helper function to print success
print_success() {
    echo -e "${GREEN}✓ $1${NC}\n"
}

# Helper function to print error
print_error() {
    echo -e "${RED}✗ $1${NC}\n"
}

# ============================================
# Authentication Examples
# ============================================

print_section "1. AUTHENTICATION"

# Register a new user
echo "Registering a new user..."
curl -X POST "${BASE_URL}/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "username": "johndoe",
    "password": "SecurePass123!"
  }' | jq '.'

print_success "User registered"

# Login
echo "Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123!"
  }')

echo "$LOGIN_RESPONSE" | jq '.'

# Extract tokens
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.refreshToken')
USER_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.userId')

print_success "Logged in successfully"
echo "Access Token: ${ACCESS_TOKEN:0:50}..."
echo "User ID: $USER_ID"

# ============================================
# User Profile Examples
# ============================================

print_section "2. USER PROFILE"

# Create user profile
echo "Creating user profile..."
curl -X POST "${BASE_URL}/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "name": "John Doe",
    "bio": "Software developer passionate about microservices",
    "avatarUrl": "https://example.com/avatars/johndoe.jpg",
    "location": "San Francisco, CA",
    "website": "https://johndoe.dev"
  }' | jq '.'

print_success "Profile created"

# Get current user profile
echo "Getting current user profile..."
curl -X GET "${BASE_URL}/users/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Profile retrieved"

# Search users
echo "Searching for users..."
curl -X GET "${BASE_URL}/users/search?query=John&page=0&size=10" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Search completed"

# ============================================
# Follow Examples
# ============================================

print_section "3. FOLLOW OPERATIONS"

# Follow a user (assuming user ID 2 exists)
echo "Following user ID 2..."
curl -X POST "${BASE_URL}/follows/2" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "User followed"

# Get follower stats
echo "Getting follower statistics..."
curl -X GET "${BASE_URL}/follows/${USER_ID}/stats" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Stats retrieved"

# Get following list
echo "Getting following list..."
curl -X GET "${BASE_URL}/follows/${USER_ID}/following?page=0&size=10" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Following list retrieved"

# Check if following a user
echo "Checking if following user 2..."
curl -X GET "${BASE_URL}/follows/check/2" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Follow status checked"

# ============================================
# Post Examples
# ============================================

print_section "4. POST OPERATIONS"

# Create a post
echo "Creating a post..."
POST_RESPONSE=$(curl -s -X POST "${BASE_URL}/posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "content": "Just deployed my first microservices architecture! 🚀",
    "mediaUrl": null
  }')

echo "$POST_RESPONSE" | jq '.'

POST_ID=$(echo "$POST_RESPONSE" | jq -r '.id')

print_success "Post created with ID: $POST_ID"

# Get post by ID
echo "Getting post by ID..."
curl -X GET "${BASE_URL}/posts/${POST_ID}" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Post retrieved"

# Get posts by user
echo "Getting posts by user..."
curl -X GET "${BASE_URL}/posts/user/${USER_ID}?page=0&size=10" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "User posts retrieved"

# Update post
echo "Updating post..."
curl -X PUT "${BASE_URL}/posts/${POST_ID}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "content": "Just deployed my first microservices architecture! 🚀 Update: Now with CI/CD!",
    "mediaUrl": null
  }' | jq '.'

print_success "Post updated"

# ============================================
# Like Examples
# ============================================

print_section "5. LIKE OPERATIONS"

# Like a post
echo "Liking post ${POST_ID}..."
curl -X POST "${BASE_URL}/likes/${POST_ID}" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Post liked"

# Get like count
echo "Getting like count..."
curl -X GET "${BASE_URL}/likes/${POST_ID}/count" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Like count retrieved"

# Check if liked
echo "Checking if post is liked..."
curl -X GET "${BASE_URL}/likes/${POST_ID}/check" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Like status checked"

# Batch like counts
echo "Getting batch like counts..."
curl -X POST "${BASE_URL}/likes/batch/counts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "postIds": [1, 2, 3]
  }' | jq '.'

print_success "Batch counts retrieved"

# Unlike post
echo "Unliking post..."
curl -X DELETE "${BASE_URL}/likes/${POST_ID}" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

print_success "Post unliked"

# ============================================
# Comment Examples
# ============================================

print_section "6. COMMENT OPERATIONS"

# Create a comment
echo "Creating a comment..."
COMMENT_RESPONSE=$(curl -s -X POST "${BASE_URL}/comments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d "{
    \"postId\": ${POST_ID},
    \"content\": \"Great post! Very informative.\"
  }")

echo "$COMMENT_RESPONSE" | jq '.'

COMMENT_ID=$(echo "$COMMENT_RESPONSE" | jq -r '.id')

print_success "Comment created with ID: $COMMENT_ID"

# Get comments for post
echo "Getting comments for post..."
curl -X GET "${BASE_URL}/comments/post/${POST_ID}?page=0&size=10" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Comments retrieved"

# Update comment
echo "Updating comment..."
curl -X PUT "${BASE_URL}/comments/${COMMENT_ID}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "content": "Great post! Very informative. Edit: Thanks for sharing!"
  }' | jq '.'

print_success "Comment updated"

# ============================================
# Feed Examples
# ============================================

print_section "7. FEED OPERATIONS"

# Get personalized feed
echo "Getting personalized feed..."
curl -X GET "${BASE_URL}/feed?page=0&size=10" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Feed retrieved"

# ============================================
# Notification Examples
# ============================================

print_section "8. NOTIFICATION OPERATIONS"

# Get all notifications
echo "Getting all notifications..."
curl -X GET "${BASE_URL}/notifications?page=0&size=10" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Notifications retrieved"

# Get unread notifications
echo "Getting unread notifications..."
curl -X GET "${BASE_URL}/notifications/unread?page=0&size=10" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "Unread notifications retrieved"

# Mark all as read
echo "Marking all notifications as read..."
curl -X PUT "${BASE_URL}/notifications/read-all" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

print_success "All notifications marked as read"

# ============================================
# Cleanup Examples
# ============================================

print_section "9. CLEANUP"

# Delete comment
echo "Deleting comment..."
curl -X DELETE "${BASE_URL}/comments/${COMMENT_ID}" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

print_success "Comment deleted"

# Delete post (soft delete)
echo "Deleting post..."
curl -X DELETE "${BASE_URL}/posts/${POST_ID}" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

print_success "Post deleted"

# Unfollow user
echo "Unfollowing user..."
curl -X DELETE "${BASE_URL}/follows/2" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

print_success "User unfollowed"

# ============================================
# Token Refresh Example
# ============================================

print_section "10. TOKEN REFRESH"

# Refresh access token
echo "Refreshing access token..."
REFRESH_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"${REFRESH_TOKEN}\"
  }")

echo "$REFRESH_RESPONSE" | jq '.'

NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESPONSE" | jq -r '.accessToken')

print_success "Token refreshed"
echo "New Access Token: ${NEW_ACCESS_TOKEN:0:50}..."

# ============================================
# Complete
# ============================================

print_section "COMPLETE"
echo "All API examples executed successfully!"
echo ""
echo "Summary:"
echo "- User ID: $USER_ID"
echo "- Access Token: ${ACCESS_TOKEN:0:30}..."
echo "- Refresh Token: ${REFRESH_TOKEN:0:30}..."
echo ""
echo "You can now use these tokens to make additional API calls."
