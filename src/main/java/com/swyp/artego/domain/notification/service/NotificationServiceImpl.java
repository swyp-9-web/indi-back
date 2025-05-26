package com.swyp.artego.domain.notification.service;

import com.swyp.artego.domain.itemEmoji.enums.EmojiType;
import com.swyp.artego.domain.notification.dto.response.NotificationListResponse;
import com.swyp.artego.domain.notification.dto.response.NotificationResponse;
import com.swyp.artego.domain.notification.entity.Notification;
import com.swyp.artego.domain.notification.enums.NotificationType;
import com.swyp.artego.domain.notification.repository.NotificationRepository;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final Long TIMEOUT = 30 * 60 * 1000L; // 30분
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public CompletableFuture<SseEmitter> subscribe(AuthUser authUser) {
        return CompletableFuture.supplyAsync(() -> {

            User user = userRepository.findByOauthId(authUser.getOauthId())
                    .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));
            Long userId = user.getId();

            SseEmitter emitter = new SseEmitter(TIMEOUT);
            emitters.put(userId, emitter);

            emitter.onCompletion(() -> {
            log.info(" SSE 완료: 유저ID={}", userId);
            emitters.remove(userId);
            });
            emitter.onTimeout(() -> {
            log.warn(" SSE 타임아웃: 유저ID={}", userId);
            emitters.remove(userId);
            });
            emitter.onError((e) -> {
            log.error(" SSE 에러 발생: 유저ID={}, error={}", userId, e.getMessage());
            emitters.remove(userId);
            });

            try {
                emitter.send(SseEmitter.event().name("connect").data("SSE 연결 완료"));
            } catch (IOException e) {
                log.info(" SSE 연결 해제: 유저ID={}", userId);
                emitters.remove(userId);
            }

            return emitter;
        }, executor);
    }

    private void sendToClient(Long receiverId, Notification notification) {
        SseEmitter emitter = emitters.get(receiverId);
        if (emitter != null) {
            synchronized (emitter) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("new-notification")
                            .data(NotificationResponse.from(notification)));
                } catch (IOException e) {
                    emitters.remove(receiverId);
                }
            }
        }
    }

    @Override
    public Notification createNotification(NotificationType type, User receiver, String content, Map<String, Object> data) {
        Notification notification = Notification.builder()
                .type(type)
                .receiver(receiver)
                .content(content)
                .data(data)
                .read(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        sendToClient(receiver.getId(), saved);
        return saved;
    }

    private String nowAsString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public void sendScrapNotification(User receiver, User sender, Long itemId, String itemTitle) {
        String timestamp = nowAsString();
        String content = String.format("[%s]님이 [%s] 작품을 스크랩했습니다. 일시: %s", sender.getNickname(), itemTitle, timestamp);
        Map<String, Object> data = Map.of(
                "sender", sender.getNickname(),
                "itemId", itemId,
                "itemTitle", itemTitle,
                "timestamp", timestamp
        );
        createNotification(NotificationType.SCRAP, receiver, content, data);
    }

    @Override
    public void sendCommentNotification(User receiver, User sender, Long itemId, String itemTitle, Long commentId) {
        String timestamp = nowAsString();
        String content = String.format("[%s]님이 [%s] 작품에 댓글을 남겼습니다. 일시: %s", sender.getNickname(), itemTitle, timestamp);
        Map<String, Object> data = Map.of(
                "senderNickname", sender.getNickname(),
                "itemId", itemId,
                "itemTitle", itemTitle,
                "commentId", commentId,
                "createdAt", timestamp
        );
        createNotification(NotificationType.COMMENT, receiver, content, data);
    }

    @Override
    public void sendReactionNotification(User receiver, User sender, Long itemId, String itemTitle, EmojiType reactionType) {
        String timestamp = nowAsString();
        String content = String.format("[%s] 님이 [%s] 작품에 [%s] 반응을 남겼습니다. 일시: %s", sender.getNickname(), itemTitle, reactionType, timestamp);
        Map<String, Object> data = Map.of(
                "sender", sender.getNickname(),
                "itemId", itemId,
                "itemTitle", itemTitle,
                "reactionType", reactionType,
                "timestamp", timestamp
        );
        createNotification(NotificationType.REACTION, receiver, content, data);
    }

    @Override
    public void sendFollowNotification(User receiver, User sender) {
        String timestamp = nowAsString();
        String content = String.format("[%s]님이 [%s]님을 팔로우했습니다. 일시: %s", sender.getNickname(), receiver.getNickname(), timestamp);
        Map<String, Object> data = Map.of(
                "sender", sender.getNickname(),
                "receiver", receiver.getNickname(),
                "timestamp", timestamp
        );
        createNotification(NotificationType.FOLLOW, receiver, content, data);
    }

    @Override
    public NotificationListResponse getUnreadNotifications(AuthUser authUser, int page, int size) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        // 프론트는 page=1부터 시작하므로, 내부에서는 0부터 시작하게 조정
        int internalPage = Math.max(page - 1, 0);
        PageRequest pageable = PageRequest.of(internalPage, size);

        Page<Notification> result = notificationRepository
                .findByReceiverIdAndReadFalseOrderByCreatedAtDesc(user.getId(), pageable);

        List<NotificationResponse> notificationResponses = result
                .getContent()
                .stream()
                .map(NotificationResponse::from)
                .toList();

        NotificationListResponse.NotificationMeta meta = NotificationListResponse.NotificationMeta.builder()
                .currentPage(page) // 프론트에 다시 1-based로 돌려줌
                .pageSize(result.getSize())
                .totalNotifications(result.getTotalElements())
                .hasNextPage(result.hasNext())
                .build();

        return NotificationListResponse.builder()
                .notifications(notificationResponses)
                .meta(meta)
                .build();
    }


    @Override
    @Transactional
    public void markAsRead(Long notificationId, AuthUser authUser) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessExceptionHandler("해당 알림이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        if (!notification.getReceiver().getId().equals(user.getId())) {
            throw new BusinessExceptionHandler("본인의 알림만 읽음 처리 가능합니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        notification.markAsRead();
    }

    @Override
    @Transactional
    public void markAllAsRead(AuthUser authUser) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        notificationRepository.markAllAsReadByReceiverId(user.getId());
    }


}
