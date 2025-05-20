package com.swyp.artego.domain.artistApply.service;

import com.swyp.artego.domain.artistApply.dto.request.ArtistApplyCreateRequest;
import com.swyp.artego.domain.artistApply.dto.request.ConvertToArtistRequest;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyCreateResponse;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyFindAllResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

import java.util.List;

public interface ArtistApplyService {

    /**
     * 작가 신청 등록
     */
    public ArtistApplyCreateResponse createArtistApply(AuthUser authUser, ArtistApplyCreateRequest request);

    /**
     * 작가 신청 전체 조회
     */
    List<ArtistApplyFindAllResponse> getArtistApplies();

    /**
     * 유저 -> 작가 전환 API
     */
    void convertUserToArtist(AuthUser adminUser, ConvertToArtistRequest request);
}


