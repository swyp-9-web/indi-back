package com.swyp.artego.domain.item.dto.response;

import com.swyp.artego.domain.item.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ItemCreateResponse {
    private Long itemId;

    /**
     * Item 엔티티 -> DTO 변환 메서드
     */
    public static ItemCreateResponse fromEntity(Item item) {
        return ItemCreateResponse.builder()
                .itemId(item.getId())
                .build();
    }
}
