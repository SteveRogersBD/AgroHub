# API Examples

This directory contains comprehensive examples for testing the Social Media Platform API.

## Files Overview

### HTTP Request Files (.http)

These files can be used with REST clients that support the `.http` format (VS Code REST Client, IntelliJ HTTP Client, etc.):

- **`auth-service.http`** - Authentication and authorization examples
  - User registration
  - Login and token management
  - Token refresh
  - Token validation

- **`user-service.http`** - User profile management
  - Create and update profiles
  - Get user profiles
  - Search users
  - Authorization examples

- **`follow-service.http`** - Follow relationship management
  - Follow/unfollow users
  - Get followers and following lists
  - Follower statistics
  - Idempotent operations

- **`post-service.http`** - Post creation and management
  - Create posts with text and media
  - Update and delete posts
  - Get posts by user
  - Soft delete examples

- **`comment-service.http`** - Comment operations
  - Create, update, and delete comments
  - Get comments for posts
  - Authorization examples

- **`like-service.http`** - Like management
  - Like/unlike posts
  - Get like counts
  - Check like status
  - Batch operations

- **`feed-service.http`** - Personalized feed
  - Get personalized feed
  - Pagination examples
  - Enriched post data

- **`notification-service.http`** - Notification management
  - Get all notifications
  - Filter unread notifications
  - Mark as read
  - Notification types

- **`complete-workflow.http`** - End-to-end user journey
  - Complete workflow from registration to content deletion
  - Step-by-step guide with all features

### Shell Script

- **`curl-examples.sh`** - Automated cURL examples
  - Bash script demonstrating all API endpoints
  - Automatic token management
  - Color-coded output
  - Complete workflow automation

### Postman Collection

- **`Social-Media-Platform.postman_collection.json`** - Postman collection
  - Import into Postman for GUI-based testing
  - Automatic token management with test scripts
  - All endpoints organized by service
  - Collection variables for easy configuration

## Using the .http Files

### Prerequisites

1. Install a REST client extension:
   - **VS Code**: [REST Client](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)
   - **IntelliJ IDEA**: Built-in HTTP Client
   - **Other editors**: Check for HTTP/REST client plugins

2. Ensure the backend services are running:
   ```bash
   cd Backend
   docker-compose up
   ```

### Getting Started

1. **Start with Authentication**:
   - Open `auth-service.http`
   - Execute the registration request
   - Execute the login request
   - Copy the `accessToken` from the response

2. **Update Token Variables**:
   - In each `.http` file, update the `@accessToken` variable:
     ```
     @accessToken = YOUR_JWT_TOKEN_HERE
     ```

3. **Execute Requests**:
   - Click "Send Request" above each request (VS Code)
   - Or use the keyboard shortcut (Ctrl+Alt+R / Cmd+Alt+R)

4. **Follow the Workflow**:
   - Use `complete-workflow.http` for a guided tour
   - Or explore individual service files

### Tips for .http Files

- **Variables**: Update variables at the top of each file
- **Comments**: Lines starting with `#` are comments
- **Separators**: `###` separates individual requests
- **Expected Responses**: Commented out for reference
- **Sequential Execution**: Some requests depend on previous ones

## Using Postman

### Prerequisites

1. **Postman** installed ([Download here](https://www.postman.com/downloads/))
2. **Backend services running**:
   ```bash
   cd Backend
   docker-compose up
   ```

### Importing the Collection

1. **Open Postman**
2. **Click Import** (top left)
3. **Select File**: Choose `Social-Media-Platform.postman_collection.json`
4. **Import**: The collection will appear in your workspace

### Using the Collection

1. **Start with Auth Service folder**:
   - Run "Register User" request
   - Run "Login" request
   - Token is automatically saved to collection variables

2. **Explore other folders**:
   - User Service
   - Follow Service
   - Post Service
   - Comment Service
   - Like Service
   - Feed Service
   - Notification Service

3. **Automatic Token Management**:
   - Login request automatically saves `accessToken`
   - All subsequent requests use this token
   - Refresh token request updates the token

### Collection Variables

The collection uses these variables (auto-populated by test scripts):
- `baseUrl`: API base URL (default: http://localhost:8080/api)
- `accessToken`: JWT access token (from login)
- `refreshToken`: JWT refresh token (from login)
- `userId`: Current user ID (from login/register)
- `postId`: Last created post ID
- `commentId`: Last created comment ID

### Tips for Postman

- **Environment**: Create an environment for different deployments (local, staging, prod)
- **Tests**: Each request has test scripts that validate responses
- **Variables**: Tokens and IDs are automatically extracted and saved
- **Sequential Execution**: Use "Run Collection" to execute all requests in order

## Using the cURL Script

### Prerequisites

1. **Bash shell** (Linux, macOS, WSL on Windows)
2. **jq** for JSON parsing:
   ```bash
   # Ubuntu/Debian
   sudo apt-get install jq
   
   # macOS
   brew install jq
   
   # Windows (WSL)
   sudo apt-get install jq
   ```

3. **Backend services running**:
   ```bash
   cd Backend
   docker-compose up
   ```

### Running the Script

1. **Make executable**:
   ```bash
   chmod +x curl-examples.sh
   ```

2. **Run the script**:
   ```bash
   ./curl-examples.sh
   ```

3. **Watch the output**:
   - The script will execute all API operations
   - Color-coded output shows success/failure
   - JSON responses are formatted with jq

### Script Features

- ✅ Automatic token management
- ✅ Sequential workflow execution
- ✅ Color-coded output
- ✅ JSON response formatting
- ✅ Error handling
- ✅ Complete API coverage

### Customizing the Script

Edit the configuration section at the top:

```bash
# Configuration
BASE_URL="http://localhost:8080/api"  # Change if using different host/port
ACCESS_TOKEN=""                        # Auto-populated by script
REFRESH_TOKEN=""                       # Auto-populated by script
USER_ID=""                            # Auto-populated by script
```

## Common Workflows

### 1. Quick Test (Authentication Only)

```bash
# Using curl
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","username":"testuser","password":"password123"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

Or use `auth-service.http` requests 1 and 3.

### 2. Create Content Workflow

1. Register and login (`auth-service.http`)
2. Create profile (`user-service.http`)
3. Create post (`post-service.http`)
4. Like your own post (`like-service.http`)
5. Comment on your post (`comment-service.http`)

### 3. Social Interaction Workflow

1. Register two users (`auth-service.http`)
2. User 1 follows User 2 (`follow-service.http`)
3. User 2 creates a post (`post-service.http`)
4. User 1 views feed (sees User 2's post) (`feed-service.http`)
5. User 1 likes and comments (`like-service.http`, `comment-service.http`)
6. User 2 checks notifications (`notification-service.http`)

### 4. Complete Platform Test

Use `complete-workflow.http` which includes:
- User registration and authentication
- Profile creation
- Following users
- Creating posts
- Viewing feeds
- Liking and commenting
- Receiving notifications
- Searching users
- Updating content
- Deleting content

## Troubleshooting

### Services Not Running

**Error**: Connection refused

**Solution**:
```bash
cd Backend
docker-compose up
```

Wait for all services to start (check health endpoints).

### Invalid Token

**Error**: 401 Unauthorized

**Solution**:
1. Login again to get a new token
2. Update the `@accessToken` variable
3. Tokens expire after 1 hour

### Token Expired

**Error**: 401 Unauthorized with "Token expired" message

**Solution**:
1. Use the refresh token endpoint
2. Or login again

### Service Not Found

**Error**: 404 Not Found on service endpoint

**Solution**:
1. Check if all services are running
2. Verify the port numbers
3. Check Docker logs: `docker-compose logs [service-name]`

### Validation Errors

**Error**: 400 Bad Request

**Solution**:
- Check the request body format
- Ensure required fields are present
- Verify field length constraints
- Check email format

## Testing Checklist

Use this checklist to verify all functionality:

- [ ] User registration
- [ ] User login
- [ ] Token refresh
- [ ] Create user profile
- [ ] Update user profile
- [ ] Search users
- [ ] Follow user
- [ ] Unfollow user
- [ ] Get followers list
- [ ] Get following list
- [ ] Create post
- [ ] Update post
- [ ] Delete post
- [ ] Get user posts
- [ ] Like post
- [ ] Unlike post
- [ ] Get like count
- [ ] Create comment
- [ ] Update comment
- [ ] Delete comment
- [ ] Get post comments
- [ ] View personalized feed
- [ ] Get notifications
- [ ] Mark notification as read
- [ ] Mark all notifications as read

## Additional Resources

- **API Guide**: See `../API_GUIDE.md` for comprehensive API documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html for interactive docs
- **Main README**: See `../README.md` for setup instructions
- **Docker Guide**: See `../DOCKER_DEPLOYMENT.md` for deployment details

## Contributing

When adding new examples:

1. Follow the existing format
2. Include expected responses as comments
3. Add error case examples
4. Update this README
5. Test all examples before committing

## Support

For issues or questions:
- Check service health: `curl http://localhost:808X/actuator/health`
- View service logs: `docker-compose logs [service-name]`
- Review the main documentation in `../API_GUIDE.md`
