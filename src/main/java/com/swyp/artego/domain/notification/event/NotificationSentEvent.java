package com.swyp.artego.domain.notification.event;

import com.swyp.artego.domain.notification.entity.Notification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationSentEvent {
    private final Notification notification;
}