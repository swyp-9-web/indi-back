package com.swyp.artego.domain.itemEmoji.dto.request;

import com.swyp.artego.domain.itemEmoji.enums.EmojiType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "아이템 이모지 생성 요청 DTO")
public class ItemEmojiCreateRequest {

    @Schema(description = "이모지를 남길 아이템 ID", example = "1")
    private Long itemId;

    @Schema(description = "이모지 타입", example = "LIKES")
    private EmojiType emojiType;


}