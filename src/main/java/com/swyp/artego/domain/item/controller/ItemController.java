package com.swyp.artego.domain.item.controller;

import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;
import com.swyp.artego.domain.item.dto.request.ItemSearchRequest;
import com.swyp.artego.domain.item.dto.response.ItemInfoResponse;
import com.swyp.artego.domain.item.dto.response.ItemSearchResultResponse;
import com.swyp.artego.domain.item.service.ItemService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * 아이템 생성 API
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createItem(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody ItemCreateRequest request) {

        itemService.createItem(user, request);

        return ResponseEntity.status(SuccessCode.INSERT_SUCCESS.getStatus())
                .body(ApiResponse.<Void>builder()
                        .result(null)
                        .resultCode(Integer.parseInt(SuccessCode.INSERT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                        .build());
    }


    /**
     * 아이템 전체 조회 API (최신순)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemInfoResponse>>> getAllItems() {
        List<ItemInfoResponse> items = itemService.getAllItems();

        return ResponseEntity.ok(
                ApiResponse.<List<ItemInfoResponse>>builder()
                        .result(items)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build()
        );
    }


    /**
     * 아이템 검색 API
     */

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ItemSearchResultResponse>> searchItems(
            @AuthenticationPrincipal AuthUser user,
            @ModelAttribute ItemSearchRequest request) {

        ItemSearchResultResponse result = itemService.searchItems(user, request);

        return ResponseEntity.ok(
                ApiResponse.<ItemSearchResultResponse>builder()
                        .result(result)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build()
        );
    }






}
