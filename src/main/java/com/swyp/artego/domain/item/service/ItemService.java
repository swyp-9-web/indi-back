package com.swyp.artego.domain.item.service;

import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;
import com.swyp.artego.domain.item.dto.request.ItemSearchRequest;
import com.swyp.artego.domain.item.dto.response.ItemCreateResponse;
import com.swyp.artego.domain.item.dto.response.ItemInfoResponse;
import com.swyp.artego.domain.item.dto.response.ItemSearchResultResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ItemService {

    /**
     * 작품을 생성한다.
     *
     * @param user
     * @param request
     * @param multipartFiles
     * @param folderName
     * @return
     */
    ItemCreateResponse createItem(AuthUser user, ItemCreateRequest request, List<MultipartFile> multipartFiles, String folderName);

    /**
     * 아이템 전체 조회 (최신순)
     */
    List<ItemInfoResponse> getAllItems();


    /**
     * 아이템 검색
     */

    ItemSearchResultResponse searchItems(AuthUser authUser, ItemSearchRequest request);
}
