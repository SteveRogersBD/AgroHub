package com.socialmedia.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationListResponse {

    private List<NotificationResponse> notifications;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
