package com.swyp.artego.domain.notification.event;

import com.swyp.artego.domain.notification.entity.Notification;
import com.swyp.artego.domain.notification.service.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationServiceImpl notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationSentEvent event) {
        Notification notification = event.getNotification();
        try {
            notificationService.sendToClient(notification.getReceiver().getId(), notification);
        } catch (Exception e) {
            log.warn("[NotificationEventListener] SSE 전송 실패: id={}, reason={}", notification.getId(), e.getMessage(), e);
        }
    }

}