package com.swyp.artego.domain.notification.repository;

import com.swyp.artego.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 1. 읽지 않은 알림 개수 (상단 뱃지 등에 사용)
    long countByReceiverIdAndReadFalse(Long receiverId);

    // 2. 알림 목록 조회 (읽음/안읽음 상관없이 모두, 최신순)
    List<Notification> findByReceiverIdOrderByReadAscCreatedAtDesc(Long receiverId, Pageable pageable);

    // 3. 읽지 않은 알림만 조회 (페이징 적용)
    Page<Notification> findByReceiverIdAndReadFalseOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    // 4. 전체 알림 조회 (페이징 포함)
    Page<Notification> findAllByReceiverId(Long receiverId, Pageable pageable);
}