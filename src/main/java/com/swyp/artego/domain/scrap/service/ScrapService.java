package com.swyp.artego.domain.scrap.service;

import com.swyp.artego.domain.scrap.dto.response.ScrapInfoResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

import java.util.List;

public interface ScrapService {

    /**
     * 스크랩 생성
     *
     * 로그인한 유저가 특정 게시글(Item)을 스크랩(좋아요)하는 기능.
     *
     * @param user   현재 인증된 유저 정보 (AuthUser)
     * @param itemId 스크랩할 게시글(Item)의 PK
     */
    void createScrap(AuthUser user, Long itemId);

    /**
     * 스크랩 전체 조회 (최신순)
     *
     * 모든 스크랩 목록을 생성일 기준으로 최신순 정렬하여 반환.
     *
     * @return 스크랩 정보 리스트 (ScrapInfoResponse)
     */
    List<ScrapInfoResponse> getAllScraps();

}
