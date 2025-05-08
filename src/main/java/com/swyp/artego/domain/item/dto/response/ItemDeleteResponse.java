package com.swyp.artego.domain.item.dto.response;

import com.swyp.artego.domain.item.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ItemDeleteResponse {

    Long itemId;

    public static ItemDeleteResponse fromEntity(Item item) {
        return ItemDeleteResponse.builder()
                .itemId(item.getId())
                .build();
    }
}
