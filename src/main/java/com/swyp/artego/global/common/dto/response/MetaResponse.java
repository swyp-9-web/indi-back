package com.swyp.artego.global.common.dto.response;

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
    private long totalItems; //TODO: totalCount 로 수정하기 ([댓글]에서도 사용함)
    private boolean hasNextPage;
}