package com.swyp.artego.domain.follow.dto.response;

import com.swyp.artego.global.common.dto.response.MetaResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FollowedArtistsResponse {

    private int totalFollowing;
    private List<FollowedArtistResponse> artists;
    private MetaResponse meta;
}