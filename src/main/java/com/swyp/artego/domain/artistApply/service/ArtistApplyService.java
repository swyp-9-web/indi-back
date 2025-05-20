package com.swyp.artego.domain.artistApply.service;

import com.swyp.artego.domain.artistApply.dto.request.ArtistApplyCreateRequest;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyCreateResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

public interface ArtistApplyService {

    /**
     * 작가 신청 등록
     */

    public ArtistApplyCreateResponse createArtistApply(AuthUser authUser, ArtistApplyCreateRequest request);
}


