package com.swyp.artego.domain.comment.entity;

import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.global.common.entity.BaseTimeEntity;
import com.swyp.artego.global.converter.BooleanToYNConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comment")
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Setter
    @Column(name = "comment", nullable = false)
    private String comment;

    @Setter
    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "secret", length = 1, nullable = false)
    private boolean secret;

    @Setter
    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "deleted", length = 1, nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;  // 이 필드가 null이면 일반 댓글, 아니면 대댓글

    @Transient // 계층형 댓글 구조를 만들기 위한 필드. DB 칼럼으로 매핑되지 않음.
    private List<Comment> replies = new ArrayList<>();

    @Builder
    public Comment(User user, Item item, String comment, boolean secret, boolean deleted, Comment parent) {
        this.user = user;
        this.item = item;
        this.comment = comment;
        this.secret = secret;
        this.deleted = deleted;
        this.parent = parent;
    }
}
