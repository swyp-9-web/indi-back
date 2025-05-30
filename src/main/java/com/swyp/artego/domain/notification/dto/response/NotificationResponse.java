package com.swyp.artego.domain.notification.dto.response;

import com.swyp.artego.domain.notification.entity.Notification;
import com.swyp.artego.domain.notification.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String content;
    private boolean read;
    private Map<String, Object> data;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .content(notification.getContent())
                .read(notification.isRead())
                .data(notification.getData())
                .createdAt(notification.getCreatedAt())
                .build();
    }

}