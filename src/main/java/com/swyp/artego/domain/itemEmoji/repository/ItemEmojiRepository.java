package com.swyp.artego.domain.itemEmoji.repository;

import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.itemEmoji.entity.ItemEmoji;
import com.swyp.artego.domain.itemEmoji.enums.EmojiType;
import com.swyp.artego.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemEmojiRepository extends JpaRepository<ItemEmoji, Long> {
    List<ItemEmoji> findAllByOrderByCreatedAtDesc();

    Optional<ItemEmoji> findByUserAndItemAndEmojiType(User user, Item item, EmojiType emojiType);

    List<ItemEmoji> findAllByUserIdAndItemId(Long userId, Long itemId);
}
