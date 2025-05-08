package com.swyp.artego.domain.item.service;

import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;
import com.swyp.artego.domain.item.dto.request.ItemSearchRequest;
import com.swyp.artego.domain.item.dto.request.ItemUpdateRequest;
import com.swyp.artego.domain.item.dto.response.*;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {

    /**
     * 작품을 생성한다.
     *
     * @param authUser
     * @param request
     * @param multipartFiles
     * @param folderName
     * @return
     */
    ItemCreateResponse createItem(AuthUser authUser, ItemCreateRequest request, List<MultipartFile> multipartFiles);

    /**
     * 작품의 세부 정보를 조회한다.
     *
     * @param itemId 작품의 id
     * @return
     */
    ItemFindByItemIdResponse findItemByItemId(Long itemId);

    /**
     * 작품을 수정한다.
     *
     * @param authUser 수정을 시도하는 사용자
     * @param itemId   수정하려는 작품의 id
     * @param request  수정 내용
     * @return ItemUpdateResponse
     */
    ItemUpdateResponse updateItem(AuthUser authUser, Long itemId, ItemUpdateRequest request, List<MultipartFile> multipartFiles);

    /**
     * 작품을 삭제한다.
     * 물리적 삭제가 아닌 statusType을 HIDE로 변경한다.(논리적 삭제)
     *
     * @param itemId 삭제하려는 작품의 id
     * @return ItemDeleteResponse
     */
    ItemDeleteResponse deleteItem(AuthUser authUser, Long itemId);

    /**
     * 아이템 전체 조회 (최신순)
     */
    List<ItemInfoResponse> getAllItems();


    /**
     * 아이템 검색
     */

    ItemSearchResultResponse searchItems(AuthUser authUser, ItemSearchRequest request);
}
