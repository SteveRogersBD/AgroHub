# Postman API Testing Guide

## Base URL
```
http://localhost:8080/api
```

## Step-by-Step Testing

### 1. Register a User

**Endpoint:** `POST /api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "testuser@example.com",
  "username": "testuser",
  "password": "Test123456!"
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "email": "testuser@example.com",
  "username": "testuser",
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### 2. Login

**Endpoint:** `POST /api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "testuser@example.com",
  "password": "Test123456!"
}
```

**Expected Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 1,
  "username": "testuser",
  "email": "testuser@example.com"
}
```

**⚠️ IMPORTANT:** Copy the `accessToken` value - you'll need it for all subsequent requests!

---

### 3. Get Feed (Empty at first)

**Endpoint:** `GET /api/feed?page=0&size=10`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Expected Response (200 OK):**
```json
{
  "content": [],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 0,
  "totalPages": 0,
  "last": true
}
```

**Note:** Feed will be empty because you haven't followed anyone yet.

---

### 4. Create a Post

**Endpoint:** `POST /api/posts`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "content": "This is my first post!",
  "mediaUrl": null
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "userId": 1,
  "content": "This is my first post!",
  "mediaUrl": null,
  "createdAt": "2024-01-15T10:35:00",
  "updatedAt": null
}
```

---

### 5. Get User's Posts

**Endpoint:** `GET /api/posts/user/1?page=0&size=10`

Replace `1` with your userId from login response.

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Expected Response (200 OK):**
```json
{
  "posts": [
    {
      "id": 1,
      "userId": 1,
      "content": "This is my first post!",
      "mediaUrl": null,
      "createdAt": "2024-01-15T10:35:00",
      "updatedAt": null
    }
  ],
  "currentPage": 0,
  "totalPages": 1,
  "totalElements": 1
}
```

---

### 6. Like a Post

**Endpoint:** `POST /api/likes/1`

Replace `1` with the post ID.

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Expected Response (204 No Content)**

---

### 7. Check Like Status

**Endpoint:** `GET /api/likes/1/check`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Expected Response (200 OK):**
```json
{
  "liked": true
}
```

---

### 8. Get Like Count

**Endpoint:** `GET /api/likes/1/count`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Expected Response (200 OK):**
```json
{
  "count": 1
}
```

---

### 9. Add a Comment

**Endpoint:** `POST /api/comments`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "postId": 1,
  "content": "Great post!"
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "postId": 1,
  "userId": 1,
  "username": "testuser",
  "userAvatarUrl": null,
  "content": "Great post!",
  "createdAt": "2024-01-15T10:40:00",
  "updatedAt": null
}
```

---

### 10. Get Post Comments

**Endpoint:** `GET /api/comments/post/1?page=0&size=10`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "postId": 1,
      "userId": 1,
      "username": "testuser",
      "userAvatarUrl": null,
      "content": "Great post!",
      "createdAt": "2024-01-15T10:40:00",
      "updatedAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

## Testing Feed with Multiple Users

To see posts in your feed, you need to follow other users:

### 11. Register Second User

**Endpoint:** `POST /api/auth/register`

**Body:**
```json
{
  "email": "user2@example.com",
  "username": "user2",
  "password": "Test123456!"
}
```

### 12. Login as Second User

**Endpoint:** `POST /api/auth/login`

**Body:**
```json
{
  "email": "user2@example.com",
  "password": "Test123456!"
}
```

Copy the new access token.

### 13. Create Post as Second User

**Endpoint:** `POST /api/posts`

**Headers:**
```
Authorization: Bearer USER2_ACCESS_TOKEN
Content-Type: application/json
```

**Body:**
```json
{
  "content": "Post from user 2!",
  "mediaUrl": null
}
```

### 14. Follow Second User (as first user)

**Endpoint:** `POST /api/follows/2`

Replace `2` with user2's ID.

**Headers:**
```
Authorization: Bearer USER1_ACCESS_TOKEN
```

**Expected Response (204 No Content)**

### 15. Get Feed (should now show user2's posts)

**Endpoint:** `GET /api/feed?page=0&size=10`

**Headers:**
```
Authorization: Bearer USER1_ACCESS_TOKEN
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": 2,
      "userId": 2,
      "username": "user2",
      "userAvatarUrl": null,
      "content": "Post from user 2!",
      "mediaUrl": null,
      "likeCount": 0,
      "commentCount": 0,
      "likedByCurrentUser": false,
      "createdAt": "2024-01-15T10:45:00",
      "updatedAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

## Common Error Responses

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header"
}
```

**Solution:** Make sure you include the Authorization header with a valid token.

### 403 Forbidden
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to perform this action"
}
```

**Solution:** You're trying to modify someone else's content.

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Post not found"
}
```

**Solution:** The resource doesn't exist or has been deleted.

### 500 Internal Server Error
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

**Solution:** Check backend logs: `docker-compose logs [service-name]`

---

## Quick Test Sequence

1. Register user → Get userId
2. Login → Get accessToken
3. Create post → Get postId
4. Like the post
5. Add comment
6. Get feed (will be empty - you haven't followed anyone)
7. Register second user
8. Login as second user
9. Create post as second user
10. Login back as first user
11. Follow second user
12. Get feed (should now show second user's post)

---

## Postman Tips

### Setting Environment Variables

1. Create a new environment in Postman
2. Add variables:
   - `baseUrl`: `http://localhost:8080/api`
   - `accessToken`: (will be set after login)
   - `userId`: (will be set after login)
   - `postId`: (will be set after creating post)

3. Use variables in requests:
   - URL: `{{baseUrl}}/feed`
   - Header: `Authorization: Bearer {{accessToken}}`

### Auto-Set Token After Login

In the login request, add this to the "Tests" tab:

```javascript
var jsonData = pm.response.json();
pm.environment.set("accessToken", jsonData.accessToken);
pm.environment.set("userId", jsonData.userId);
```

This will automatically save the token for subsequent requests.

---

## Troubleshooting

### Services Not Responding

Check if services are running:
```powershell
cd Backend
docker-compose ps
```

All services should show "Up" status.

### Connection Refused

Make sure Docker services are started:
```powershell
docker-compose up -d
```

### Token Expired

Tokens expire after 1 hour. If you get 401 errors, login again to get a new token.

---

## Complete API List

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | /api/auth/register | Register new user | No |
| POST | /api/auth/login | Login user | No |
| POST | /api/auth/refresh | Refresh access token | No |
| GET | /api/users/{id} | Get user by ID | Yes |
| GET | /api/users/me | Get current user | Yes |
| POST | /api/follows/{userId} | Follow user | Yes |
| DELETE | /api/follows/{userId} | Unfollow user | Yes |
| GET | /api/follows/{userId}/followers | Get followers | Yes |
| GET | /api/follows/{userId}/following | Get following | Yes |
| POST | /api/posts | Create post | Yes |
| GET | /api/posts/{id} | Get post by ID | Yes |
| GET | /api/posts/user/{userId} | Get user's posts | Yes |
| PUT | /api/posts/{id} | Update post | Yes |
| DELETE | /api/posts/{id} | Delete post | Yes |
| POST | /api/comments | Create comment | Yes |
| GET | /api/comments/post/{postId} | Get post comments | Yes |
| PUT | /api/comments/{id} | Update comment | Yes |
| DELETE | /api/comments/{id} | Delete comment | Yes |
| POST | /api/likes/{postId} | Like post | Yes |
| DELETE | /api/likes/{postId} | Unlike post | Yes |
| GET | /api/likes/{postId}/check | Check if liked | Yes |
| GET | /api/likes/{postId}/count | Get like count | Yes |
| GET | /api/feed | Get personalized feed | Yes |
| GET | /api/notifications | Get notifications | Yes |

---

## Next Steps

1. Test each endpoint in order
2. Note which ones fail
3. Check the error messages
4. Share the results so I can help fix any issues

Good luck testing! 🚀
