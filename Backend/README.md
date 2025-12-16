# Social Media Backend - Microservices

This is a Maven multi-module project for a social media platform backend built with Java 21 and Spring Boot 3.2.x.

## Project Structure

```
Backend/
├── pom.xml                    # Parent POM with shared dependencies
├── api-gateway/               # API Gateway service
├── auth-service/              # Authentication service
├── user-service/              # User profile management
├── follow-service/            # Follow relationships
├── post-service/              # Post management
├── comment-service/           # Comment management
├── like-service/              # Like management
├── feed-service/              # Feed generation
└── notification-service/      # Notification management
```

## Technology Stack

- **Java**: 21 (LTS)
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
- **Database**: PostgreSQL 16
- **Security**: JWT (jjwt 0.12.3)
- **API Documentation**: SpringDoc OpenAPI 3
- **Testing**: JUnit 5, Testcontainers, jqwik (Property-Based Testing)
- **Build Tool**: Maven 3.9.x

## Shared Dependencies

All services inherit the following dependencies from the parent POM:

- Spring Boot Starter (Web, Data JPA, Validation)
- PostgreSQL Driver (42.7.1)
- JWT Library (jjwt 0.12.3)
- SpringDoc OpenAPI (2.3.0)
- Lombok (1.18.30)
- Testcontainers (1.19.3)
- jqwik for Property-Based Testing (1.8.2)

## Quick Start

**New to the platform?** See [GETTING_STARTED.md](GETTING_STARTED.md) for a step-by-step tutorial.

### Using Docker (Recommended)

The fastest way to get started:

```bash
# 1. Start all services
docker-compose up -d

# 2. Wait for services to be healthy (30-60 seconds)
# Check health: curl http://localhost:8080/actuator/health

# 3. Try the API
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","username":"user","password":"password123"}'

# 4. Explore with examples
# See api-examples/ directory for comprehensive examples
```

### Using API Examples

```bash
# Option 1: Use .http files with VS Code REST Client
# Open api-examples/complete-workflow.http and follow the steps

# Option 2: Import Postman collection
# Import api-examples/Social-Media-Platform.postman_collection.json

# Option 3: Run automated curl script
cd api-examples
chmod +x curl-examples.sh
./curl-examples.sh
```

See [api-examples/README.md](api-examples/README.md) for detailed instructions.

## Building the Project

### Prerequisites

- Java 21 or higher
- Maven 3.9.x (or use the included Maven wrapper)

### Build All Services

```bash
# Using Maven wrapper (recommended)
./mvnw.cmd clean install

# Or using system Maven
mvn clean install
```

### Build Individual Service

```bash
# Using Maven wrapper
./mvnw.cmd clean install -pl auth-service

# Or using system Maven
mvn clean install -pl auth-service
```

### Validate Project Structure

```bash
./mvnw.cmd validate
```

## Service Descriptions

### API Gateway
- Routes requests to appropriate microservices
- JWT token validation
- CORS configuration
- Centralized error handling

### Auth Service
- User registration and authentication
- JWT token generation and validation
- Password hashing with BCrypt
- Refresh token management

### User Service
- User profile CRUD operations
- User search by name/username
- Profile information management

### Follow Service
- Follow/unfollow operations
- Followers and following lists
- Follower statistics

### Post Service
- Post creation, update, and deletion
- Soft delete implementation
- Post retrieval with pagination

### Comment Service
- Comment CRUD operations
- Comment retrieval by post
- Comment authorization

### Like Service
- Like/unlike operations
- Like count tracking
- Batch like count queries

### Feed Service
- Personalized feed generation
- Aggregates posts from followed users
- Enriches posts with metadata

### Notification Service
- Notification creation and management
- Unread notification filtering
- Mark as read functionality

## Configuration

Each service requires its own `application.properties` or `application.yml` file with:

- Database connection settings
- Server port configuration
- JWT secret (for auth-service and api-gateway)
- Service URLs (for inter-service communication)

## Database Schema

Each service (except feed-service) has its own PostgreSQL database schema:

- `auth_db` - Auth Service
- `user_db` - User Service
- `follow_db` - Follow Service
- `post_db` - Post Service
- `comment_db` - Comment Service
- `like_db` - Like Service
- `notification_db` - Notification Service

## Testing

The project includes:

- **Unit Tests**: JUnit 5 with Mockito
- **Integration Tests**: Testcontainers for PostgreSQL
- **Property-Based Tests**: jqwik for testing universal properties

Run all tests:

```bash
./mvnw.cmd test
```

## Docker Deployment

The project includes Docker configuration for all services. See [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) for detailed instructions.

### Quick Start with Docker

```bash
# Build and start all services
docker-compose up --build

# Start in detached mode
docker-compose up -d

# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

### Service Ports

- API Gateway: http://localhost:8080
- Auth Service: http://localhost:8081
- User Service: http://localhost:8082
- Follow Service: http://localhost:8083
- Post Service: http://localhost:8084
- Comment Service: http://localhost:8085
- Like Service: http://localhost:8086
- Feed Service: http://localhost:8087
- Notification Service: http://localhost:8088

### Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
cp .env.example .env
```

Key variables:
- `JWT_SECRET`: Secret key for JWT token signing (required)

## API Documentation

### Comprehensive API Guide

See [API_GUIDE.md](API_GUIDE.md) for complete API documentation including:
- Authentication flows
- All endpoint details with examples
- Error handling
- Best practices
- Common patterns

### Interactive API Examples

The `api-examples/` directory contains `.http` files for testing all endpoints:

- `auth-service.http` - User registration and authentication
- `user-service.http` - User profile management
- `follow-service.http` - Follow/unfollow operations
- `post-service.http` - Post creation and management
- `comment-service.http` - Comment operations
- `like-service.http` - Like/unlike operations
- `feed-service.http` - Personalized feed
- `notification-service.http` - Notification management
- `complete-workflow.http` - Complete user journey from registration to content creation

**Using .http files:**
- Open in VS Code with REST Client extension
- Or use any HTTP client that supports .http format
- Update the `@accessToken` variable after login

### Swagger UI Documentation

Each service provides interactive Swagger UI documentation at `/swagger-ui.html`:

- API Gateway: http://localhost:8080/swagger-ui.html
- Auth Service: http://localhost:8081/swagger-ui.html
- User Service: http://localhost:8082/swagger-ui.html
- Follow Service: http://localhost:8083/swagger-ui.html
- Post Service: http://localhost:8084/swagger-ui.html
- Comment Service: http://localhost:8085/swagger-ui.html
- Like Service: http://localhost:8086/swagger-ui.html
- Feed Service: http://localhost:8087/swagger-ui.html
- Notification Service: http://localhost:8088/swagger-ui.html

## Health Checks

All services expose health check endpoints at `/actuator/health`:

```bash
# Check auth service health
curl http://localhost:8081/actuator/health

# Check all services
for port in {8080..8088}; do
  echo "Service on port $port:"
  curl http://localhost:$port/actuator/health
  echo ""
done
```

## License

[Add your license information here]
