# Docker Deployment Guide

This guide explains how to build and run the Social Media Backend microservices using Docker and Docker Compose.

## Prerequisites

- Docker 20.10 or higher
- Docker Compose 2.0 or higher
- At least 4GB of available RAM
- At least 10GB of available disk space

## Quick Start

### 1. Clone the Repository

```bash
cd Backend
```

### 2. Configure Environment Variables (Optional)

Copy the example environment file and customize if needed:

```bash
cp .env.example .env
```

Edit `.env` to set your JWT secret and other configuration:

```bash
JWT_SECRET=your-secure-256-bit-secret-key-here
```

### 3. Build and Start All Services

```bash
docker-compose up --build
```

This command will:
- Build Docker images for all 9 microservices
- Start 7 PostgreSQL database containers
- Start all microservices with proper dependencies
- Create a shared network for inter-service communication

### 4. Verify Services are Running

Check the status of all containers:

```bash
docker-compose ps
```

All services should show as "Up" or "healthy".

### 5. Access the Services

- **API Gateway**: http://localhost:8080
- **Auth Service**: http://localhost:8081
- **User Service**: http://localhost:8082
- **Follow Service**: http://localhost:8083
- **Post Service**: http://localhost:8084
- **Comment Service**: http://localhost:8085
- **Like Service**: http://localhost:8086
- **Feed Service**: http://localhost:8087
- **Notification Service**: http://localhost:8088

### 6. Access API Documentation

Each service provides Swagger UI documentation:

- Auth Service: http://localhost:8081/swagger-ui.html
- User Service: http://localhost:8082/swagger-ui.html
- Follow Service: http://localhost:8083/swagger-ui.html
- Post Service: http://localhost:8084/swagger-ui.html
- Comment Service: http://localhost:8085/swagger-ui.html
- Like Service: http://localhost:8086/swagger-ui.html
- Feed Service: http://localhost:8087/swagger-ui.html
- Notification Service: http://localhost:8088/swagger-ui.html
- API Gateway: http://localhost:8080/swagger-ui.html

## Service Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway (8080)                       │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐     ┌──────────────┐
│Auth Service  │      │User Service  │     │Post Service  │
│   (8081)     │      │   (8082)     │     │   (8084)     │
│   auth-db    │      │   user-db    │     │   post-db    │
└──────────────┘      └──────────────┘     └──────────────┘
        │                     │                     │
        │                     ▼                     ▼
        │             ┌──────────────┐     ┌──────────────┐
        │             │Follow Service│     │Comment Svc   │
        │             │   (8083)     │     │   (8085)     │
        │             │  follow-db   │     │  comment-db  │
        │             └──────────────┘     └──────────────┘
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐     ┌──────────────┐
│Like Service  │      │Feed Service  │     │Notification  │
│   (8086)     │      │   (8087)     │     │   Service    │
│   like-db    │      │  (no DB)     │     │   (8088)     │
└──────────────┘      └──────────────┘     │notification-db│
                                            └──────────────┘
```

## Docker Commands

### Start Services in Detached Mode

```bash
docker-compose up -d
```

### Stop All Services

```bash
docker-compose down
```

### Stop and Remove Volumes (Clean Slate)

```bash
docker-compose down -v
```

### View Logs

View logs for all services:
```bash
docker-compose logs -f
```

View logs for a specific service:
```bash
docker-compose logs -f auth-service
```

### Rebuild a Specific Service

```bash
docker-compose up -d --build auth-service
```

### Scale a Service (if needed)

```bash
docker-compose up -d --scale user-service=3
```

### Execute Commands in a Container

```bash
docker-compose exec auth-service bash
```

### Check Service Health

```bash
docker-compose ps
```

## Database Access

Each service has its own PostgreSQL database. You can connect to them using:

### Auth Database
```bash
docker-compose exec auth-db psql -U postgres -d auth_db
```

### User Database
```bash
docker-compose exec user-db psql -U postgres -d user_db
```

### Follow Database
```bash
docker-compose exec follow-db psql -U postgres -d follow_db
```

### Post Database
```bash
docker-compose exec post-db psql -U postgres -d post_db
```

### Comment Database
```bash
docker-compose exec comment-db psql -U postgres -d comment_db
```

### Like Database
```bash
docker-compose exec like-db psql -U postgres -d like_db
```

### Notification Database
```bash
docker-compose exec notification-db psql -U postgres -d notification_db
```

## Environment Variables

The following environment variables can be configured in the `.env` file:

### Required
- `JWT_SECRET`: Secret key for JWT token signing (256-bit recommended)

### Optional (with defaults)
- Database credentials (default: postgres/postgres)
- Service ports (defaults: 8080-8088)
- Service URLs for inter-service communication

## Health Checks

All services include health check endpoints that Docker monitors:

- Interval: 30 seconds
- Timeout: 3 seconds
- Start period: 40 seconds
- Retries: 3

Health check endpoint: `/actuator/health`

## Networking

All services communicate through a Docker bridge network named `social-media-network`. This allows:

- Service discovery by container name
- Isolated network communication
- Secure inter-service communication

## Volumes

Persistent data is stored in Docker volumes:

- `auth-db-data`: Auth service database
- `user-db-data`: User service database
- `follow-db-data`: Follow service database
- `post-db-data`: Post service database
- `comment-db-data`: Comment service database
- `like-db-data`: Like service database
- `notification-db-data`: Notification service database

## Troubleshooting

### Services Won't Start

1. Check if ports are already in use:
```bash
netstat -an | grep LISTEN | grep -E "808[0-8]|543[2-8]"
```

2. Check Docker logs:
```bash
docker-compose logs
```

3. Ensure Docker has enough resources (4GB RAM minimum)

### Database Connection Issues

1. Verify database containers are healthy:
```bash
docker-compose ps
```

2. Check database logs:
```bash
docker-compose logs auth-db
```

3. Restart the database container:
```bash
docker-compose restart auth-db
```

### Service Communication Issues

1. Verify all services are on the same network:
```bash
docker network inspect social-media-network
```

2. Check service logs for connection errors:
```bash
docker-compose logs feed-service
```

### Out of Memory

1. Increase Docker memory limit in Docker Desktop settings
2. Reduce the number of running services
3. Clear unused Docker resources:
```bash
docker system prune -a
```

## Production Considerations

For production deployment, consider:

1. **Security**:
   - Use strong JWT secrets
   - Enable HTTPS/TLS
   - Implement rate limiting
   - Use secrets management (e.g., Docker Secrets, Vault)

2. **Scalability**:
   - Use container orchestration (Kubernetes, Docker Swarm)
   - Implement load balancing
   - Use managed database services
   - Add caching layers (Redis)

3. **Monitoring**:
   - Add logging aggregation (ELK Stack)
   - Implement metrics collection (Prometheus)
   - Set up alerting (Grafana)
   - Add distributed tracing (Jaeger)

4. **High Availability**:
   - Run multiple instances of each service
   - Use database replication
   - Implement circuit breakers
   - Add health check endpoints

5. **CI/CD**:
   - Automate Docker image builds
   - Implement automated testing
   - Use image scanning for vulnerabilities
   - Implement rolling deployments

## Development Workflow

### Making Code Changes

1. Make changes to the service code
2. Rebuild the specific service:
```bash
docker-compose up -d --build auth-service
```

3. View logs to verify changes:
```bash
docker-compose logs -f auth-service
```

### Running Tests

Tests should be run before building Docker images:

```bash
# Run all tests
mvn clean test

# Run tests for a specific service
cd auth-service
mvn test
```

### Debugging

To debug a service:

1. Stop the service:
```bash
docker-compose stop auth-service
```

2. Run the service locally with your IDE debugger

3. Ensure the service can connect to the Docker databases:
```bash
# Update application.yml to use localhost:5432 instead of auth-db:5432
```

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)

## Support

For issues or questions:
1. Check the logs: `docker-compose logs`
2. Review this documentation
3. Check the main README.md for project-specific information
