package com.swyp.artego.domain.itemEmoji.service;

import com.swyp.artego.domain.itemEmoji.dto.request.ItemEmojiCreateRequest;

import com.swyp.artego.domain.itemEmoji.dto.response.ItemEmojiInfoResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

import java.util.List;

public interface ItemEmojiService {

    /**
     * 아이템 이모지 생성
     *
     * @param user    현재 로그인한 유저 정보
     * @param request 이모지 생성 요청 DTO
     */
    Long createItemEmoji(AuthUser user, ItemEmojiCreateRequest request);

    /**
     * 전체 아이템 이모지 조회 (최신순)
     *
     * @return 이모지 목록
     */
    List<ItemEmojiInfoResponse> getAllItemEmojis();



    /**
     * 이모지 삭제
     *
     */

    void deleteItemEmojiById(Long itemEmojiId);
}
