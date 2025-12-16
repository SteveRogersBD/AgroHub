# Security Architecture Refactoring

## Overview
This document describes the refactoring of the microservices security architecture to centralize authentication at the API Gateway level.

## Changes Made

### Architecture Pattern
**Before:** Each microservice validated JWT tokens independently
**After:** Gateway-only authentication with header-based user context propagation

### Benefits
1. **Performance**: JWT validation happens once at the gateway instead of in every service
2. **Simplicity**: Removed security dependencies from 8 microservices
3. **Maintainability**: Single point of authentication logic
4. **Scalability**: Reduced overhead in downstream services

## Implementation Details

### 1. API Gateway (No Changes Needed)
- **Keeps**: Spring Security, JWT dependencies
- **Responsibility**: 
  - Validates JWT tokens from clients
  - Extracts user information (userId, email, roles)
  - Adds headers to forwarded requests: `X-User-Id`, `X-User-Email`, `X-User-Roles`

### 2. Auth Service (No Changes Needed)
- **Keeps**: Spring Security, JWT dependencies, BCrypt
- **Responsibility**:
  - Handles user registration and login
  - Generates JWT tokens
  - Validates passwords

### 3. All Other Services (post, comment, like, feed, notification, user, follow, media)
- **Removed**: 
  - Spring Security dependency
  - JWT dependencies (jjwt-api, jjwt-impl, jjwt-jackson)
  - SecurityConfig classes
  - JwtAuthenticationFilter classes
  - JwtTokenProvider classes
  
- **Added**:
  - OpenFeign dependency for inter-service communication
  - `@EnableFeignClients` annotation on main application class

- **Updated**:
  - Controllers now use `@RequestHeader("X-User-Id") Long userId` instead of `Authentication authentication`
  - No more `authentication.getPrincipal()` calls

## Request Flow

### Client Login
```
Client → Gateway → Auth-Service (validates credentials, creates JWT) → Gateway → Client
```

### Protected Request
```
Client (with JWT) → Gateway (validates JWT, adds X-User-Id header) → Service (reads X-User-Id) → Gateway → Client
```

### Service-to-Service Communication
```
Service A → Service B (via OpenFeign, passes X-User-Id header)
```

## Security Considerations

### Network Isolation
Services should be deployed in a private network where:
- Only the gateway is publicly accessible
- Services cannot be accessed directly from outside
- Docker Compose or Kubernetes network policies enforce this

### Header Trust
Services trust the `X-User-Id` header because:
- It can only come from the gateway (network isolation)
- Gateway validates JWT before adding the header
- No external client can bypass the gateway

### Future Enhancements (Optional)
For additional security, consider:
1. **Header Signing**: Gateway signs the X-User-Id header with a shared secret
2. **mTLS**: Mutual TLS between services
3. **Service Mesh**: Use Istio/Linkerd for automatic security

## Migration Checklist

### Completed ✅
- [x] Added OpenFeign to parent pom.xml
- [x] Removed Security/JWT dependencies from 8 services
- [x] Deleted SecurityConfig files from services (8 files)
- [x] Deleted JwtAuthenticationFilter files from services (8 files)
- [x] Deleted JwtTokenProvider files from services (7 files)
- [x] Added @EnableFeignClients to all service applications (8 services)
- [x] Updated post-service controller to use headers
- [x] Updated comment-service controller to use headers
- [x] Updated like-service controller to use headers
- [x] Updated follow-service controller to use headers
- [x] Updated user-service controller to use headers
- [x] Updated notification-service controller to use headers
- [x] Updated feed-service controller to use headers
- [x] Updated media-service controller to use headers

### Remaining (Frontend & Testing)
- [ ] Test all backend services
- [ ] Update Android app to store JWT in SharedPreferences
- [ ] Update Android app to add JWT to all API calls
- [ ] Integration testing through gateway

## Testing

### Unit Tests
Controllers can be tested by passing the X-User-Id header:
```java
mockMvc.perform(post("/api/posts")
    .header("X-User-Id", "123")
    .content(requestBody))
```

### Integration Tests
Test through the gateway to ensure JWT validation works:
```bash
# Login to get JWT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# Use JWT in subsequent requests
curl -X GET http://localhost:8080/api/posts \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

## Rollback Plan
If issues arise:
1. Revert pom.xml changes
2. Restore deleted security files from git history
3. Revert controller changes
4. Rebuild and redeploy services

## Production Deployment
1. Deploy all services simultaneously
2. Ensure network isolation is configured
3. Monitor gateway logs for authentication errors
4. Monitor service logs for missing header errors
5. Update client applications to use new authentication flow
