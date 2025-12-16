package com.socialmedia.media.controller;

import com.socialmedia.media.dto.ErrorResponse;
import com.socialmedia.media.dto.UploadResponse;
import com.socialmedia.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Media Management", description = "APIs for uploading and managing media files")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload an image",
            description = "Upload an image file to Google Cloud Storage. Returns the public URL.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<UploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            UploadResponse response = mediaService.uploadImage(file, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error uploading file", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @DeleteMapping("/{fileName}")
    @Operation(
            summary = "Delete an image",
            description = "Delete an image from Google Cloud Storage",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Void> deleteImage(@PathVariable String fileName) {
        try {
            mediaService.deleteImage(fileName);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting file", e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(IllegalArgumentException e) {
        ErrorResponse error = ErrorResponse.builder()
                .message(e.getMessage())
                .error("Validation Error")
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        ErrorResponse error = ErrorResponse.builder()
                .message(e.getMessage())
                .error("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
