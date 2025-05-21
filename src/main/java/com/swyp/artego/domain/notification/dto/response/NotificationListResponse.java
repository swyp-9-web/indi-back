package com.swyp.artego.domain.notification.dto.response;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationListResponse {
    private List<NotificationResponse> notifications;
    private NotificationMeta meta;

    @Getter
    @Builder
    public static class NotificationMeta {
        private int currentPage;
        private int pageSize;
        private long totalNotifications;
        private boolean hasNextPage;
    }


}
