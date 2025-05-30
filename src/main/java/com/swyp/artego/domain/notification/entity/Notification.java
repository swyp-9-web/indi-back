package com.swyp.artego.domain.notification.entity;

import com.swyp.artego.domain.notification.enums.NotificationType;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.global.common.entity.BaseTimeEntity;
import com.swyp.artego.global.converter.BooleanToYNConverter;
import com.swyp.artego.global.converter.JsonMapConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noti_id", nullable = false)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "noti_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "content", nullable = false)
    private String content;
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "json", nullable = false)
    private Map<String, Object> data;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "is_read", length = 1, nullable = false)
    private boolean read = false;

    @Builder
    private Notification(NotificationType type, User receiver, String content, Map<String, Object> data, boolean read) {
        this.type = type;
        this.receiver = receiver;
        this.content = content;
        this.data = data;
        this.read = read;
    }

    public void markAsRead() {
        this.read = true;
    }


}