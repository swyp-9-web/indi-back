package com.swyp.artego.domain.notification.controller;

import com.swyp.artego.domain.notification.dto.response.NotificationListResponse;
import com.swyp.artego.domain.notification.service.NotificationService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notification", description = "알림 관련 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE 알림 구독")
    public SseEmitter subscribe(@AuthenticationPrincipal AuthUser authUser) {
        return notificationService.subscribe(authUser);
    }


    @GetMapping("/unread")
    @Operation(summary = "안읽은 알림 목록 조회")
    public ResponseEntity<ApiResponse<NotificationListResponse>> getUnreadNotifications(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        NotificationListResponse response = notificationService.getUnreadNotifications(authUser, page, size);

        return ResponseEntity.status(SuccessCode.SELECT_SUCCESS.getStatus())
                .body(ApiResponse.<NotificationListResponse>builder()
                        .result(response)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build());
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "특정 알림 읽음 처리")
    public ResponseEntity<ApiResponse<String>> markNotificationAsRead(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long notificationId) {

        notificationService.markAsRead(notificationId, authUser);

        return ResponseEntity.status(SuccessCode.UPDATE_SUCCESS.getStatus())
                .body(ApiResponse.<String>builder()
                        .result("알림이 읽음 처리되었습니다.")
                        .resultCode(Integer.parseInt(SuccessCode.UPDATE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.UPDATE_SUCCESS.getMessage())
                        .build());
    }

    @PatchMapping("/read/all")
    @Operation(summary = "모든 안읽은 알림 읽음 처리")
    public ResponseEntity<ApiResponse<String>> markAllNotificationsAsRead(
            @AuthenticationPrincipal AuthUser authUser) {

        notificationService.markAllAsRead(authUser);

        return ResponseEntity.status(SuccessCode.UPDATE_SUCCESS.getStatus())
                .body(ApiResponse.<String>builder()
                        .result("모든 알림이 읽음 처리되었습니다.")
                        .resultCode(Integer.parseInt(SuccessCode.UPDATE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.UPDATE_SUCCESS.getMessage())
                        .build());
    }
}
