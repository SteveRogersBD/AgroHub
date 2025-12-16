# OpenFeign Inter-Service Communication Example

## Overview
OpenFeign is now configured in all services for inter-service communication. Here's how to use it.

## Example: Post Service Calling User Service

### 1. Create Feign Client Interface

```java
// Backend/post-service/src/main/java/com/socialmedia/post/client/UserServiceClient.java
package com.socialmedia.post.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${services.user-service.url:http://localhost:8082}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    UserProfileResponse getUserProfile(
        @PathVariable Long id,
        @RequestHeader("X-User-Id") Long requestingUserId
    );
}
```

### 2. Create DTO (if not exists)

```java
// Backend/post-service/src/main/java/com/socialmedia/post/client/UserProfileResponse.java
package com.socialmedia.post.client;

import lombok.Data;

@Data
public class UserProfileResponse {
    private Long id;
    private Long userId;
    private String username;
    private String displayName;
    private String bio;
    private String profilePictureUrl;
}
```

### 3. Use in Service

```java
// Backend/post-service/src/main/java/com/socialmedia/post/service/PostService.java
package com.socialmedia.post.service;

import com.socialmedia.post.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    
    private final UserServiceClient userServiceClient;
    
    public PostWithUserResponse getPostWithUserDetails(Long postId, Long requestingUserId) {
        // Get post
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("Post not found"));
        
        // Call user-service to get user details
        UserProfileResponse userProfile = userServiceClient.getUserProfile(
            post.getUserId(), 
            requestingUserId
        );
        
        // Combine and return
        return PostWithUserResponse.builder()
            .post(post)
            .userProfile(userProfile)
            .build();
    }
}
```

### 4. Configuration (application.yml)

```yaml
# Backend/post-service/src/main/resources/application.yml
services:
  user-service:
    url: http://localhost:8082
  # Add other services as needed
```

## Example: Feed Service Calling Multiple Services

```java
// Backend/feed-service/src/main/java/com/socialmedia/feed/client/PostServiceClient.java
package com.socialmedia.feed.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "post-service", url = "${services.post-service.url:http://localhost:8083}")
public interface PostServiceClient {
    
    @GetMapping("/api/posts/user/{userId}")
    PostListResponse getPostsByUserId(
        @PathVariable Long userId,
        @RequestParam int page,
        @RequestParam int size,
        @RequestHeader("X-User-Id") Long requestingUserId
    );
}
```

```java
// Backend/feed-service/src/main/java/com/socialmedia/feed/client/FollowServiceClient.java
package com.socialmedia.feed.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "follow-service", url = "${services.follow-service.url:http://localhost:8084}")
public interface FollowServiceClient {
    
    @GetMapping("/api/follows/{userId}/following")
    FollowingListResponse getFollowing(
        @PathVariable Long userId,
        @RequestHeader("X-User-Id") Long requestingUserId
    );
}
```

## Error Handling

### Global Feign Error Decoder

```java
// Backend/post-service/src/main/java/com/socialmedia/post/config/FeignErrorDecoder.java
package com.socialmedia.post.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {
    
    private final ErrorDecoder defaultDecoder = new Default();
    
    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 404:
                return new NotFoundException("Resource not found in " + methodKey);
            case 401:
                return new UnauthorizedException("Unauthorized access to " + methodKey);
            case 403:
                return new ForbiddenException("Forbidden access to " + methodKey);
            default:
                return defaultDecoder.decode(methodKey, response);
        }
    }
}
```

## Request Interceptor (Pass Headers)

```java
// Backend/post-service/src/main/java/com/socialmedia/post/config/FeignRequestInterceptor.java
package com.socialmedia.post.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // Forward X-User-Id header
            String userId = request.getHeader("X-User-Id");
            if (userId != null) {
                template.header("X-User-Id", userId);
            }
            
            // Forward other headers if needed
            String userEmail = request.getHeader("X-User-Email");
            if (userEmail != null) {
                template.header("X-User-Email", userEmail);
            }
        }
    }
}
```

## Configuration Class

```java
// Backend/post-service/src/main/java/com/socialmedia/post/config/FeignConfig.java
package com.socialmedia.post.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Change to BASIC in production
    }
}
```

## Testing Feign Clients

```java
// Backend/post-service/src/test/java/com/socialmedia/post/client/UserServiceClientTest.java
package com.socialmedia.post.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
class UserServiceClientTest {
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Test
    void shouldGetUserProfile() {
        // Mock response
        stubFor(get(urlEqualTo("/api/users/1"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":1,\"username\":\"testuser\"}")));
        
        // Call
        UserProfileResponse response = userServiceClient.getUserProfile(1L, 1L);
        
        // Verify
        assertThat(response.getUsername()).isEqualTo("testuser");
    }
}
```

## Best Practices

1. **Always pass X-User-Id header** in inter-service calls
2. **Use DTOs** specific to Feign clients (don't reuse entity classes)
3. **Handle errors** with custom error decoder
4. **Configure timeouts** appropriately
5. **Use circuit breakers** (Resilience4j) for fault tolerance
6. **Log requests** in development, reduce in production
7. **Version your APIs** to avoid breaking changes

## Common Issues

### Issue: Connection Refused
**Solution**: Check service URL in application.yml

### Issue: 401 Unauthorized
**Solution**: Ensure X-User-Id header is being passed

### Issue: Timeout
**Solution**: Configure timeout in application.yml:
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
```

## Next Steps

1. Create Feign clients as needed for your use cases
2. Add circuit breakers with Resilience4j
3. Implement retry logic for transient failures
4. Monitor inter-service calls with distributed tracing (Zipkin/Jaeger)
