package com.swyp.artego.domain.notification.repository;

import com.swyp.artego.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 읽지 않은 알림들을 최신순으로 페이징 조회합니다.
     */
    Page<Notification> findByReceiverIdAndReadFalseOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    /**
     * 해당 유저의 모든 읽지 않은 알림을 '읽음(true)' 상태로 일괄 업데이트합니다.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.receiver.id = :receiverId AND n.read = false")
    int markAllAsReadByReceiverId(@Param("receiverId") Long receiverId);
}