package com.swyp.artego.domain.item.dto.response;

import com.swyp.artego.domain.item.entity.Item;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemUpdateResponse {

    private Long itemId;

    public static ItemUpdateResponse fromEntity(Item item) {
        return ItemUpdateResponse.builder()
                .itemId(item.getId())
                .build();
    }
}
