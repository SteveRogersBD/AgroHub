package com.socialmedia.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostListResponse {

    private List<PostResponse> posts;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
