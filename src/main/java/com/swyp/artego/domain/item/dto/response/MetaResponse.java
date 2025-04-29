package com.swyp.artego.domain.item.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaResponse {
    private int currentPage;
    private int pageSize;
    private long totalItems;
    private boolean hasNextPage;
}