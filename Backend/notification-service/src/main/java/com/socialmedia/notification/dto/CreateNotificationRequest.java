package com.socialmedia.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Actor ID is required")
    private Long actorId;

    private Long entityId;

    @NotBlank(message = "Message is required")
    private String message;
}
