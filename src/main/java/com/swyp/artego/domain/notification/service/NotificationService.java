package com.swyp.artego.domain.notification.service;

import com.swyp.artego.domain.itemEmoji.enums.EmojiType;
import com.swyp.artego.domain.notification.dto.response.NotificationListResponse;
import com.swyp.artego.domain.notification.entity.Notification;
import com.swyp.artego.domain.notification.enums.NotificationType;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface NotificationService {

    /**
     * 1. SSE 구독 요청 처리
     * - 클라이언트가 /sse/subscribe/{userId}로 구독 요청을 보내면
     * - 서버는 SseEmitter를 생성하여 클라이언트와 연결을 유지함
     * - 503 방지를 위해 연결 즉시 더미 이벤트("connect")를 전송함
     */
    CompletableFuture<SseEmitter> subscribe(AuthUser authUser);
    /**
     * 2. 알림 DB 저장 및 실시간 전송
     * - 알림을 저장하고
     * - 해당 유저가 SSE 구독 중이면 실시간으로 알림을 push
     */

    Notification createNotification(NotificationType type, User receiver, String content, Map<String, Object> data);

    /**
     * 3. 스크랩 알림 생성
     * - 어떤 유저가 어떤 작품을 스크랩했는지 알림
     */
    void sendScrapNotification(User receiver, User sender, Long itemId, String itemTitle);

    /**
     * 4. 댓글 알림 생성
     * - 어떤 유저가 어떤 작품에 댓글을 남겼는지 알림
     */
    void sendCommentNotification(User receiver, User sender, Long itemId, String itemTitle, Long commentId);

    /**
     * 5. 이모지 반응 알림 생성
     * - 어떤 유저가 어떤 작품에 어떤 반응을 남겼는지 알림
     */
    void sendReactionNotification(User receiver, User sender, Long itemId, String itemTitle, EmojiType reactionType);

    /**
     * 6. 팔로우 알림 생성
     * - 어떤 유저가 나를 팔로우 했을 때 알림
     */
    void sendFollowNotification(User receiver, User sender);

    /**
     * 7. 읽지 않은 알림 목록 조회
     * - 로그인 시 읽지 않은 알림 목록 조회
     */

    NotificationListResponse getUnreadNotifications(AuthUser receiver, int page, int size);


    /**
     * 8. 읽지 않은 알림 읽음으로 변경
     * - 알림 클릭 시 읽음으로 변경
     */
    void markAsRead(Long notificationId, AuthUser authUser);


    /**
     * 8. 읽지 않은 알림 읽음으로 변경 [전체]
     * - 알림 클릭 시 읽음으로 변경 [전체]
     */
    void markAllAsRead(AuthUser authUser);
}
