package com.swyp.artego.domain.item.service;

import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;
import com.swyp.artego.domain.item.dto.response.ItemInfoResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

import java.util.List;

public interface ItemService {

    /**
     * 아이템 생성
     */
    void createItem(AuthUser user, ItemCreateRequest request);

    /**
     * 아이템 전체 조회 (최신순)
     */
    List<ItemInfoResponse> getAllItems();
}
