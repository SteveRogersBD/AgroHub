package com.socialmedia.feed.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedResponse {

    @JsonProperty("content")
    private List<EnrichedPostResponse> posts;
    
    @JsonProperty("pageable")
    private PageableInfo pageable;
    
    private long totalElements;
    private int totalPages;
    
    @JsonProperty("last")
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
