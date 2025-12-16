package com.socialmedia.notification.mapper;

import com.socialmedia.notification.dto.CreateNotificationRequest;
import com.socialmedia.notification.dto.NotificationResponse;
import com.socialmedia.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .actorId(notification.getActorId())
                .entityId(notification.getEntityId())
                .message(notification.getMessage())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public Notification toEntity(CreateNotificationRequest request) {
        if (request == null) {
            return null;
        }

        return Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .actorId(request.getActorId())
                .entityId(request.getEntityId())
                .message(request.getMessage())
                .read(false)
                .build();
    }
}
