package com.swyp.artego.domain.scrap.service;

import com.swyp.artego.global.auth.oauth.model.AuthUser;

public interface ScrapService {

    /**
     * 스크랩 생성
     *
     * @param user   로그인한 유저 정보
     * @param itemId 스크랩할 아이템의 ID
     */
    void createScrap(AuthUser user, Long itemId);

    /**
     * 스크랩 취소
     *
     * @param user   로그인한 유저 정보
     * @param itemId 스크랩을 취소할 아이템의 ID
     */
    void deleteScrap(AuthUser user, Long itemId);
}
