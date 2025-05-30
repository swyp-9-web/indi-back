package com.swyp.artego.domain.follow.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FollowPreviewListResponse {

    private int totalFollowings;
    private List<FollowPreviewResponse> followingArtists;

}