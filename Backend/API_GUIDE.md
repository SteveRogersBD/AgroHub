# Social Media Platform API Guide

This guide provides comprehensive documentation for using the Social Media Platform API.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Authentication](#authentication)
3. [API Endpoints](#api-endpoints)
4. [Common Patterns](#common-patterns)
5. [Error Handling](#error-handling)
6. [Rate Limiting](#rate-limiting)
7. [Best Practices](#best-practices)

## Getting Started

### Base URLs

- **API Gateway** (recommended): `http://localhost:8080/api`
- **Direct Service Access** (for development):
  - Auth Service: `http://localhost:8081/api/auth`
  - User Service: `http://localhost:8082/api/users`
  - Follow Service: `http://localhost:8083/api/follows`
  - Post Service: `http://localhost:8084/api/posts`
  - Comment Service: `http://localhost:8085/api/comments`
  - Like Service: `http://localhost:8086/api/likes`
  - Feed Service: `http://localhost:8087/api/feed`
  - Notification Service: `http://localhost:8088/api/notifications`

### Prerequisites

1. Ensure all services are running (see [README.md](README.md) for setup instructions)
2. Use Docker Compose for easy local development: `docker-compose up`
3. Have a REST client ready (Postman, Insomnia, VS Code REST Client, or curl)

## Authentication

### Registration Flow

**Endpoint:** `POST /api/auth/register`

**Request:**
```json
{
  "email": "user@example.com",
  "username": "username",
  "password": "SecurePass123!"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "email": "user@example.com",
  "username": "username",
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00"
}
```

**Validation Rules:**
- Email must be valid format and unique
- Username must be unique
- Password must be at least 8 characters
- Default role is USER

### Login Flow

**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 1,
  "username": "username",
  "email": "user@example.com"
}
```

**Token Lifetimes:**
- Access Token: 1 hour
- Refresh Token: 7 days

### Using Access Tokens

Include the access token in the Authorization header for all protected endpoints:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Refreshing Tokens

**Endpoint:** `POST /api/auth/refresh`

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

## API Endpoints

### User Service

#### Create/Update Profile

**Create:** `POST /api/users`  
**Update:** `PUT /api/users/{id}`

```json
{
  "name": "John Doe",
  "bio": "Software developer",
  "avatarUrl": "https://example.com/avatar.jpg",
  "location": "San Francisco, CA",
  "website": "https://example.com"
}
```

#### Get Profile

- **By ID:** `GET /api/users/{id}`
- **By Username:** `GET /api/users/username/{username}`
- **Current User:** `GET /api/users/me`

#### Search Users

**Endpoint:** `GET /api/users/search?query={query}&page={page}&size={size}`

**Parameters:**
- `query`: Search term (searches name and username)
- `page`: Page number (0-indexed)
- `size`: Items per page (default: 10)

### Follow Service

#### Follow/Unfollow

- **Follow:** `POST /api/follows/{userId}`
- **Unfollow:** `DELETE /api/follows/{userId}`

Both operations are idempotent.

#### Get Relationships

- **Followers:** `GET /api/follows/{userId}/followers?page={page}&size={size}`
- **Following:** `GET /api/follows/{userId}/following?page={page}&size={size}`
- **Stats:** `GET /api/follows/{userId}/stats`
- **Check:** `GET /api/follows/check/{userId}`

### Post Service

#### Create Post

**Endpoint:** `POST /api/posts`

```json
{
  "content": "Post content here",
  "mediaUrl": "https://example.com/image.jpg"
}
```

**Validation:**
- Content required if no media URL
- Content max length: 5000 characters
- Media URL is optional

#### Update Post

**Endpoint:** `PUT /api/posts/{id}`

```json
{
  "content": "Updated content",
  "mediaUrl": "https://example.com/new-image.jpg"
}
```

**Authorization:** Only the post author can update

#### Delete Post

**Endpoint:** `DELETE /api/posts/{id}`

**Note:** This is a soft delete. The post is marked as deleted but not removed from the database.

#### Get Posts

- **By ID:** `GET /api/posts/{id}`
- **By User:** `GET /api/posts/user/{userId}?page={page}&size={size}`

### Comment Service

#### Create Comment

**Endpoint:** `POST /api/comments`

```json
{
  "postId": 1,
  "content": "Great post!"
}
```

**Validation:**
- Content required
- Content max length: 2000 characters
- Post must exist

#### Update Comment

**Endpoint:** `PUT /api/comments/{id}`

```json
{
  "content": "Updated comment"
}
```

**Authorization:** Only the comment author can update

#### Delete Comment

**Endpoint:** `DELETE /api/comments/{id}`

**Authorization:** Only the comment author can delete

#### Get Comments

**Endpoint:** `GET /api/comments/post/{postId}?page={page}&size={size}`

Returns comments in chronological order (oldest first).

### Like Service

#### Like/Unlike

- **Like:** `POST /api/likes/{postId}`
- **Unlike:** `DELETE /api/likes/{postId}`

Both operations are idempotent.

#### Get Like Information

- **Count:** `GET /api/likes/{postId}/count`
- **Check:** `GET /api/likes/{postId}/check`
- **Batch Counts:** `POST /api/likes/batch/counts`

**Batch Request:**
```json
{
  "postIds": [1, 2, 3, 4, 5]
}
```

**Batch Response:**
```json
{
  "counts": {
    "1": 5,
    "2": 12,
    "3": 3,
    "4": 0,
    "5": 8
  }
}
```

### Feed Service

#### Get Personalized Feed

**Endpoint:** `GET /api/feed?page={page}&size={size}`

**Parameters:**
- `page`: Page number (0-indexed)
- `size`: Items per page (default: 10)

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "userId": 2,
      "username": "janesmith",
      "userAvatarUrl": "https://example.com/avatar.jpg",
      "content": "Post content",
      "mediaUrl": null,
      "likeCount": 12,
      "commentCount": 3,
      "likedByCurrentUser": true,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 25,
  "totalPages": 3,
  "last": false
}
```

**Features:**
- Shows posts from users you follow
- Ordered by creation time (newest first)
- Excludes soft-deleted posts
- Includes enriched metadata (like count, comment count, liked status)

### Notification Service

#### Get Notifications

- **All:** `GET /api/notifications?page={page}&size={size}`
- **Unread:** `GET /api/notifications/unread?page={page}&size={size}`

#### Mark as Read

- **Single:** `PUT /api/notifications/{id}/read`
- **All:** `PUT /api/notifications/read-all`

#### Notification Types

- **LIKE:** Someone liked your post
- **COMMENT:** Someone commented on your post
- **FOLLOW:** Someone followed you

**Note:** Self-actions (liking your own post, etc.) do not create notifications.

## Common Patterns

### Pagination

All list endpoints support pagination with these parameters:

- `page`: Page number (0-indexed, default: 0)
- `size`: Items per page (default: 10, max: 100)

**Response Format:**
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 50,
  "totalPages": 5,
  "last": false
}
```

### Idempotent Operations

These operations can be called multiple times with the same result:

- Follow (won't create duplicate relationships)
- Unfollow (won't fail if not following)
- Like (won't create duplicate likes)
- Unlike (won't fail if not liked)

### Soft Deletes

Posts use soft delete:
- Deleted posts are marked as deleted but not removed
- Deleted posts don't appear in queries
- Deleted posts cannot be retrieved by ID

Comments use hard delete:
- Deleted comments are permanently removed

## Error Handling

### Standard Error Response

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/posts"
}
```

### HTTP Status Codes

- **200 OK:** Successful GET, PUT requests
- **201 Created:** Successful POST requests
- **204 No Content:** Successful DELETE requests
- **400 Bad Request:** Validation errors, invalid input
- **401 Unauthorized:** Missing or invalid authentication
- **403 Forbidden:** Insufficient permissions
- **404 Not Found:** Resource not found
- **409 Conflict:** Duplicate resource (e.g., email already exists)
- **500 Internal Server Error:** Server-side errors

### Common Error Scenarios

#### Authentication Errors

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials"
}
```

#### Authorization Errors

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You can only update your own posts"
}
```

#### Validation Errors

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Content must not exceed 5000 characters"
}
```

#### Not Found Errors

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Post not found"
}
```

## Rate Limiting

Currently, rate limiting is not implemented but is planned for future releases.

**Planned Limits:**
- 100 requests per minute per user
- 1000 requests per hour per user

## Best Practices

### 1. Always Use the API Gateway

Use `http://localhost:8080/api` instead of direct service URLs for:
- Centralized authentication
- Consistent error handling
- Future rate limiting support

### 2. Handle Token Expiration

- Access tokens expire after 1 hour
- Implement automatic token refresh
- Store refresh tokens securely

### 3. Implement Pagination

- Always use pagination for list endpoints
- Start with reasonable page sizes (10-20 items)
- Don't request excessively large pages

### 4. Cache Appropriately

Consider caching:
- User profiles (update infrequently)
- Like counts (can tolerate slight staleness)
- Follower counts (can tolerate slight staleness)

Don't cache:
- Feeds (personalized and time-sensitive)
- Notifications (real-time updates important)

### 5. Batch Operations

Use batch endpoints when available:
- `POST /api/likes/batch/counts` for multiple like counts
- Reduces number of HTTP requests

### 6. Error Handling

- Always check HTTP status codes
- Parse error messages for user feedback
- Implement retry logic for 5xx errors
- Don't retry 4xx errors (client errors)

### 7. Security

- Never log or expose JWT tokens
- Store tokens securely (not in localStorage for web apps)
- Use HTTPS in production
- Validate all user input on the client side

### 8. Testing

Use the provided `.http` files in `api-examples/` directory:
- `auth-service.http` - Authentication flows
- `user-service.http` - User profile management
- `follow-service.http` - Follow relationships
- `post-service.http` - Post management
- `comment-service.http` - Comment operations
- `like-service.http` - Like operations
- `feed-service.http` - Feed generation
- `notification-service.http` - Notifications
- `complete-workflow.http` - End-to-end workflow

## Example Workflows

### Complete User Journey

See `api-examples/complete-workflow.http` for a step-by-step guide through:

1. User registration and authentication
2. Profile creation
3. Following other users
4. Creating posts
5. Viewing personalized feed
6. Liking posts
7. Commenting on posts
8. Receiving notifications
9. Searching for users
10. Updating content
11. Deleting content

### Quick Start

```bash
# 1. Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","username":"user","password":"password123"}'

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# 3. Create a post (use token from login)
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"content":"My first post!","mediaUrl":null}'

# 4. Get your feed
curl -X GET "http://localhost:8080/api/feed?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## API Documentation (Swagger)

Interactive API documentation is available via Swagger UI:

- **API Gateway:** http://localhost:8080/swagger-ui.html
- **Auth Service:** http://localhost:8081/swagger-ui.html
- **User Service:** http://localhost:8082/swagger-ui.html
- **Follow Service:** http://localhost:8083/swagger-ui.html
- **Post Service:** http://localhost:8084/swagger-ui.html
- **Comment Service:** http://localhost:8085/swagger-ui.html
- **Like Service:** http://localhost:8086/swagger-ui.html
- **Feed Service:** http://localhost:8087/swagger-ui.html
- **Notification Service:** http://localhost:8088/swagger-ui.html

## Support

For issues, questions, or contributions:
- Check the main [README.md](README.md)
- Review [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) for deployment issues
- Check service health endpoints: `http://localhost:808X/actuator/health`

## Version

API Version: 1.0.0  
Last Updated: January 2024
