package com.socialmedia.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Paginated user search response")
public class UserSearchResponse {

    @Schema(description = "List of user profiles matching search criteria")
    private List<UserProfileResponse> users;
    
    @Schema(description = "Current page number", example = "0")
    private int currentPage;
    
    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;
    
    @Schema(description = "Total number of matching users", example = "100")
    private long totalElements;
    
    @Schema(description = "Number of items per page", example = "20")
    private int pageSize;
}
