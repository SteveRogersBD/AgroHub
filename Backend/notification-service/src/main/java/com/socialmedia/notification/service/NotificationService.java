package com.socialmedia.notification.service;

import com.socialmedia.notification.dto.CreateNotificationRequest;
import com.socialmedia.notification.dto.NotificationListResponse;
import com.socialmedia.notification.dto.NotificationResponse;
import com.socialmedia.notification.entity.Notification;
import com.socialmedia.notification.exception.BadRequestException;
import com.socialmedia.notification.exception.ResourceNotFoundException;
import com.socialmedia.notification.mapper.NotificationMapper;
import com.socialmedia.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        // Filter to prevent self-notifications
        if (request.getUserId().equals(request.getActorId())) {
            throw new BadRequestException("Cannot create notification for self-action");
        }

        Notification notification = notificationMapper.toEntity(request);
        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.toResponse(savedNotification);
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<NotificationResponse> notifications = notificationPage.getContent().stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        return NotificationListResponse.builder()
                .notifications(notifications)
                .currentPage(notificationPage.getNumber())
                .totalPages(notificationPage.getTotalPages())
                .totalItems(notificationPage.getTotalElements())
                .pageSize(notificationPage.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getUnreadNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository
                .findByUserIdAndReadOrderByCreatedAtDesc(userId, false, pageable);

        List<NotificationResponse> notifications = notificationPage.getContent().stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        return NotificationListResponse.builder()
                .notifications(notifications)
                .currentPage(notificationPage.getNumber())
                .totalPages(notificationPage.getTotalPages())
                .totalItems(notificationPage.getTotalElements())
                .pageSize(notificationPage.getSize())
                .build();
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // Verify the notification belongs to the user
        if (!notification.getUserId().equals(userId)) {
            throw new BadRequestException("Cannot mark another user's notification as read");
        }

        notification.setRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        return notificationMapper.toResponse(updatedNotification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndReadOrderByCreatedAtDesc(userId, false, pageable);

        unreadNotifications.getContent().forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
}
