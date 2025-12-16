package com.socialmedia.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResponse {
    private String url;
    private String fileName;
    private String contentType;
    private Long size;
}
