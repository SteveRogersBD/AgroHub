# Community Feed Error Fix

## Problem
When navigating to the Community tab in the Android app after login, the app showed an error:
```
Required value 'content' missing $
```

The app loaded for some time and then displayed this Moshi JSON parsing error.

## Root Cause
There were TWO major issues:

### Issue 1: Response Structure Mismatch
The backend's `FeedResponse` structure didn't match what the Android app expected:

**Android App Expected (PagedResponseDto):**
```json
{
  "content": [...],           // List of items
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false
}
```

**Backend Returned (FeedResponse):**
```json
{
  "posts": [...],             // Wrong field name!
  "page": 0,                  // Wrong structure!
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

### Issue 2: Missing User Information
The backend's `EnrichedPostResponse` was missing user information fields:

**Android App Expected (FeedPostDto):**
- `username` (String)
- `userAvatarUrl` (String)

**Backend Returned (EnrichedPostResponse):**
- Only `userId` (Long)
- Missing `username` and `userAvatarUrl`

Both mismatches caused Moshi to fail parsing the JSON response.

## Solution

### 1. Fixed FeedResponse Structure
Updated `FeedResponse` to match Android app's expected format:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedResponse {
    @JsonProperty("content")              // Changed from "posts"
    private List<EnrichedPostResponse> posts;
    
    @JsonProperty("pageable")             // NEW: Nested object
    private PageableInfo pageable;
    
    private long totalElements;
    private int totalPages;
    
    @JsonProperty("last")                 // NEW: Last page indicator
    private boolean last;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageableInfo {
        private int pageNumber;
        private int pageSize;
    }
}
```

### 2. Added User Information to EnrichedPostResponse
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedPostResponse {
    private Long id;
    private Long userId;
    private String username;        // NEW
    private String userAvatarUrl;   // NEW
    private String content;
    private String mediaUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long likeCount;
    private Long commentCount;
    private Boolean likedByCurrentUser;
}
```

### 3. Created UserServiceClient
Created a new client to fetch user profile information:
- File: `Backend/feed-service/src/main/java/com/socialmedia/feed/client/UserServiceClient.java`
- Calls: `GET /api/users/user/{userId}` endpoint
- Returns: `UserProfileResponse` with name and avatarUrl

### 4. Updated FeedService
Modified the service to:
1. Build proper pageable structure in responses
2. Extract unique user IDs from posts
3. Batch fetch user profiles using UserServiceClient
4. Include username and avatarUrl in each EnrichedPostResponse

### 5. Updated Tests
- Updated `FeedServicePropertiesTest.java` to include UserServiceClient
- Updated `FeedServiceIntegrationTest.java` to use new pageable structure

## Files Changed
1. `Backend/feed-service/src/main/java/com/socialmedia/feed/dto/FeedResponse.java` - Fixed response structure
2. `Backend/feed-service/src/main/java/com/socialmedia/feed/dto/EnrichedPostResponse.java` - Added user fields
3. `Backend/feed-service/src/main/java/com/socialmedia/feed/client/UserServiceClient.java` (NEW)
4. `Backend/feed-service/src/main/java/com/socialmedia/feed/dto/UserProfileResponse.java` (NEW)
5. `Backend/feed-service/src/main/java/com/socialmedia/feed/service/FeedService.java` - Updated to build proper responses
6. `Backend/feed-service/src/test/java/com/socialmedia/feed/properties/FeedServicePropertiesTest.java` - Updated tests
7. `Backend/feed-service/src/test/java/com/socialmedia/feed/integration/FeedServiceIntegrationTest.java` - Updated tests

## Deployment
The feed-service has been rebuilt and redeployed using Docker Compose:
```bash
docker-compose up -d --build feed-service
```

## Testing
To test the fix:
1. Login to the Android app
2. Navigate to the Community tab
3. The feed should now load successfully with user information displayed
4. Posts should show username and avatar for each author

## Next Steps
If you still see issues:
1. Check that the user-service is running and accessible
2. Verify that user profiles exist for the post authors
3. Check feed-service logs: `docker logs feed-service`
4. Ensure the Android app is pointing to the correct backend URL
