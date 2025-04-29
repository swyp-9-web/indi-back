package com.swyp.artego.domain.comment.entity;

import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.global.common.entity.BaseTimeEntity;
import com.swyp.artego.global.converter.BooleanToYNConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comment")
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="item_id")
    private Item item;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "secret", length = 1, nullable = false)
    private boolean secret;

    @Builder
    public Comment(User user, Item item, String comment, boolean secret) {
        this.user = user;
        this.item = item;
        this.comment = comment;
        this.secret = secret;
    }
}
