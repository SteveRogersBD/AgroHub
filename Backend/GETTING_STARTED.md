# Getting Started with Social Media Platform API

This guide will help you get up and running with the Social Media Platform API in minutes.

## Prerequisites

- Docker and Docker Compose installed
- (Optional) Postman, VS Code with REST Client, or curl

## Step 1: Start the Services

```bash
cd Backend
docker-compose up -d
```

Wait 30-60 seconds for all services to start. Check health:

```bash
curl http://localhost:8080/actuator/health
```

You should see: `{"status":"UP"}`

## Step 2: Register and Login

### Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "username": "john",
    "password": "password123"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Save the `accessToken` from the response!** You'll need it for authenticated requests.

## Step 3: Create Your Profile

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "name": "John Doe",
    "bio": "Software developer",
    "avatarUrl": "https://example.com/avatar.jpg",
    "location": "San Francisco, CA",
    "website": "https://johndoe.dev"
  }'
```

## Step 4: Create a Post

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "content": "My first post! 🚀",
    "mediaUrl": null
  }'
```

## Step 5: Explore the API

### View Your Feed

```bash
curl -X GET "http://localhost:8080/api/feed?page=0&size=10" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Follow a User

```bash
curl -X POST http://localhost:8080/api/follows/2 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Like a Post

```bash
curl -X POST http://localhost:8080/api/likes/1 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Comment on a Post

```bash
curl -X POST http://localhost:8080/api/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "postId": 1,
    "content": "Great post!"
  }'
```

### Check Notifications

```bash
curl -X GET "http://localhost:8080/api/notifications?page=0&size=10" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Next Steps

### Option 1: Use Interactive Examples

**Postman:**
1. Import `api-examples/Social-Media-Platform.postman_collection.json`
2. Run requests with automatic token management

**VS Code REST Client:**
1. Install REST Client extension
2. Open `api-examples/complete-workflow.http`
3. Follow the step-by-step workflow

### Option 2: Run Automated Script

```bash
cd api-examples
chmod +x curl-examples.sh
./curl-examples.sh
```

This script demonstrates all API functionality automatically.

### Option 3: Explore Swagger UI

Visit http://localhost:8080/swagger-ui.html for interactive API documentation.

## Common Tasks

### Register Multiple Users

To test social features, register multiple users:

```bash
# User 1
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user1@example.com","username":"user1","password":"password123"}'

# User 2
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user2@example.com","username":"user2","password":"password123"}'
```

### Test Social Interactions

1. User 1 follows User 2
2. User 2 creates a post
3. User 1 views feed (sees User 2's post)
4. User 1 likes and comments
5. User 2 checks notifications

See `api-examples/complete-workflow.http` for the complete flow.

### Refresh Your Token

Tokens expire after 1 hour. Refresh them:

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

## Troubleshooting

### Services Not Starting

```bash
# Check service status
docker-compose ps

# View logs
docker-compose logs

# Restart services
docker-compose restart
```

### Token Expired

Login again to get a new token or use the refresh endpoint.

### Connection Refused

Ensure services are running and healthy:

```bash
# Check all service health
for port in {8080..8088}; do
  echo "Port $port:"
  curl -s http://localhost:$port/actuator/health | jq '.status'
done
```

### Database Issues

Reset the database:

```bash
docker-compose down -v
docker-compose up -d
```

## Architecture Overview

The platform consists of 9 microservices:

1. **API Gateway** (8080) - Entry point for all requests
2. **Auth Service** (8081) - Authentication and JWT management
3. **User Service** (8082) - User profiles
4. **Follow Service** (8083) - Follow relationships
5. **Post Service** (8084) - Post management
6. **Comment Service** (8085) - Comments
7. **Like Service** (8086) - Likes
8. **Feed Service** (8087) - Personalized feeds
9. **Notification Service** (8088) - Notifications

**Always use the API Gateway (port 8080) for client requests.**

## Key Concepts

### Authentication

- Register → Login → Get JWT token
- Include token in Authorization header: `Bearer YOUR_TOKEN`
- Access tokens expire after 1 hour
- Refresh tokens expire after 7 days

### Pagination

Most list endpoints support pagination:
- `page`: Page number (0-indexed)
- `size`: Items per page (default: 10)

Example: `GET /api/feed?page=0&size=20`

### Idempotent Operations

These operations can be called multiple times safely:
- Follow (won't create duplicates)
- Unfollow (won't fail if not following)
- Like (won't create duplicates)
- Unlike (won't fail if not liked)

### Soft Deletes

Posts use soft delete:
- Deleted posts are marked as deleted
- They don't appear in queries
- Data is preserved for analytics

## Resources

- **Complete API Guide**: [API_GUIDE.md](API_GUIDE.md)
- **API Examples**: [api-examples/](api-examples/)
- **Docker Deployment**: [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)
- **Main README**: [README.md](README.md)
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## Quick Reference

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints
```
POST /auth/register    - Register new user
POST /auth/login       - Login
POST /auth/refresh     - Refresh token
```

### Main Endpoints
```
POST   /users                      - Create profile
GET    /users/me                   - Get current user
GET    /users/search               - Search users
POST   /follows/{userId}           - Follow user
GET    /follows/{userId}/stats     - Get follow stats
POST   /posts                      - Create post
GET    /posts/user/{userId}        - Get user posts
POST   /likes/{postId}             - Like post
POST   /comments                   - Create comment
GET    /feed                       - Get personalized feed
GET    /notifications              - Get notifications
```

## Support

For detailed documentation, see:
- [API_GUIDE.md](API_GUIDE.md) - Complete API reference
- [api-examples/README.md](api-examples/README.md) - Example usage
- Service logs: `docker-compose logs [service-name]`

Happy coding! 🚀
