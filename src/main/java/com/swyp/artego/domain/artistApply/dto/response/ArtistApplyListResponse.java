package com.swyp.artego.domain.artistApply.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ArtistApplyListResponse {
    private List<ArtistApplyFindAllResponse> applies;
    private Meta meta;

    @Getter
    @Builder
    public static class Meta {
        private int currentPage;
        private int pageSize;
        private long totalApplies;
        private boolean hasNextPage;
    }
}