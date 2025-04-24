package com.swyp.artego.domain.follow.service;

import com.swyp.artego.domain.follow.dto.response.FollowInfoResponse;
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
     * 팔로우 전체 조회 (최신순)
     *
     * @return 팔로우 목록
     */
    List<FollowInfoResponse> getAllFollows();
}
