package com.swyp.artego.domain.item.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSearchResultResponse {

    private List<ItemSearchResponse> items;
    private MetaResponse meta;
    private ConditionsResponse conditions;
}
