package com.swyp.artego.domain.itemEmoji.repository;

import com.swyp.artego.domain.itemEmoji.entity.ItemEmoji;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemEmojiRepository extends JpaRepository<ItemEmoji, Long> {
    List<ItemEmoji> findAllByOrderByCreatedAtDesc();
}
