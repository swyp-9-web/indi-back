package com.swyp.artego.domain.follow.service;

import com.swyp.artego.domain.follow.dto.response.FollowInfoResponse;
import com.swyp.artego.domain.follow.dto.response.FollowPreviewListResponse;
import com.swyp.artego.domain.follow.dto.response.FollowedArtistsResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

import java.util.List;

public interface FollowService {

    /**
     * 팔로우 생성
     *
     * @param user 로그인한 유저
     * @param artistId 팔로우할 아티스트(유저) ID
     */
    void createFollow(AuthUser user, Long artistId);


    /**
     * 팔로우 프리뷰
     *
     * @param user 로그인한 유저
     * return FollowPreviewListResponse 팔로우 프리뷰
     */

    FollowPreviewListResponse getFollowPreview(AuthUser user);



    /**
     * 팔로우한 작가 및 작품 목록 조회
     *
     * @param page 페이지 번호
     * @param size 페이지 당 아이템 count
     * return FollowedArtistsResponse 유저가 팔로우한 작가 및 작품 목록
     */
    FollowedArtistsResponse getFollowedArtists(AuthUser authUser, int page, int size);



    /**
     * 팔로우 삭제
     *
     */
    void deleteFollow(AuthUser authUser, Long artistId);


}
