package com.swyp.artego.domain.artistApply.service;

import com.swyp.artego.domain.artistApply.dto.request.ArtistApplyCreateRequest;
import com.swyp.artego.domain.artistApply.dto.request.ConvertToArtistRequest;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyCreateResponse;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyFindAllResponse;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyListResponse;
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
    ArtistApplyListResponse getArtistApplies(int page, int size);

    /**
     * 작가 신청 승인
     */
    void convertUserToArtist(AuthUser adminUser, ConvertToArtistRequest request);

    /**
     * 작가 신청 거절
     */
    void rejectArtistApply(AuthUser adminUser, ConvertToArtistRequest request);
}


