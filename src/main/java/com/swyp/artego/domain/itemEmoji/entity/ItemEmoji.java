package com.swyp.artego.domain.itemEmoji.entity;

import com.swyp.artego.domain.item.entity.Item;
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
@Table(name = "item_emoji")
public class ItemEmoji extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_emoji_id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name="item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Column(name = "emoji_type", nullable = false)
    private String emojiType;

    @Builder
    public ItemEmoji(Item item, User user, String emojiType) {
        this.item = item;
        this.user = user;
        this.emojiType = emojiType;
    }
}
