package com.swyp.artego.domain.item.repository;

import com.swyp.artego.domain.item.dto.request.ItemSearchRequest;

import com.swyp.artego.domain.item.dto.response.ItemSearchResultResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

import java.util.List;

public interface ItemSearchRepository {

    ItemSearchResultResponse searchItems (AuthUser authUser, ItemSearchRequest request);
}
