package com.swyp.artego.domain.post.entity;

import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.global.common.entity.BaseTimeEntity;
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

    @Column(name = "secret", length = 1, nullable = false)
    // isSecret = "N" or "Y"
    private String isSecret;

    @Builder
    public Comment(User user, Item item, String comment, String isSecret) {
        this.user = user;
        this.item = item;
        this.comment = comment;
        this.isSecret = isSecret;
    }
}
