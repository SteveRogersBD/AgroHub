package com.socialmedia.media.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.socialmedia.media.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final Storage storage;

    @Value("${gcs.bucket-name}")
    private String bucketName;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    public UploadResponse uploadImage(MultipartFile file, Long userId) throws IOException {
        // Validate file
        validateFile(file);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = String.format("posts/%d/%s%s", 
                userId, 
                UUID.randomUUID().toString(), 
                extension);

        // Upload to GCS
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        Blob blob = storage.create(blobInfo, file.getBytes());

        // Generate public URL
        String publicUrl = String.format("https://storage.googleapis.com/%s/%s", 
                bucketName, 
                fileName);

        log.info("File uploaded successfully: {}", publicUrl);

        return UploadResponse.builder()
                .url(publicUrl)
                .fileName(fileName)
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();
    }

    public void deleteImage(String fileName) {
        try {
            BlobId blobId = BlobId.of(bucketName, fileName);
            boolean deleted = storage.delete(blobId);
            
            if (deleted) {
                log.info("File deleted successfully: {}", fileName);
            } else {
                log.warn("File not found: {}", fileName);
            }
        } catch (Exception e) {
            log.error("Error deleting file: {}", fileName, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }

        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_CONTENT_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
