package com.swyp.artego.domain.itemEmoji.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.swyp.artego.domain.itemEmoji.entity.ItemEmoji;
import com.swyp.artego.domain.itemEmoji.enums.EmojiType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ItemEmojiInfoResponse {

    private Long id;
    private Long itemId;
    private String userName;
    private EmojiType emojiType;


    private LocalDateTime createdAt;

    public static ItemEmojiInfoResponse fromEntity(ItemEmoji itemEmoji) {
        return ItemEmojiInfoResponse.builder()
                .id(itemEmoji.getId())
                .itemId(itemEmoji.getItem().getId())
                .userName(itemEmoji.getUser().getName())
                .emojiType(itemEmoji.getEmojiType())
                .createdAt(itemEmoji.getCreatedAt())
                .build();
    }


}