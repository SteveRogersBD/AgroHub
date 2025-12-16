package com.socialmedia.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String type;
    private Long actorId;
    private Long entityId;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;
}
