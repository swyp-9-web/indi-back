package com.swyp.artego.domain.follow.repository;

import com.swyp.artego.domain.follow.dto.response.FollowedArtistsResponse;


public interface FollowSearchRepository {
    FollowedArtistsResponse findFollowedArtistsWithItems(Long userId, Integer page , Integer size);
}
