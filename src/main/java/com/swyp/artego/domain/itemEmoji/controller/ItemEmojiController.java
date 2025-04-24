package com.swyp.artego.domain.itemEmoji.controller;

import com.swyp.artego.domain.itemEmoji.dto.request.ItemEmojiCreateRequest;
import com.swyp.artego.domain.itemEmoji.dto.response.ItemEmojiInfoResponse;
import com.swyp.artego.domain.itemEmoji.service.ItemEmojiService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/item-emojis")
@RequiredArgsConstructor
public class ItemEmojiController {

    private final ItemEmojiService itemEmojiService;

    /**
     * 이모지 생성 API
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createItemEmoji(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody ItemEmojiCreateRequest request) {

        itemEmojiService.createItemEmoji(user, request);

        return ResponseEntity.status(SuccessCode.INSERT_SUCCESS.getStatus())
                .body(ApiResponse.<Void>builder()
                        .result(null)
                        .resultCode(Integer.parseInt(SuccessCode.INSERT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                        .build());
    }

    /**
     * 이모지 전체 조회 API (최신순)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemEmojiInfoResponse>>> getAllItemEmojis() {
        List<ItemEmojiInfoResponse> emojis = itemEmojiService.getAllItemEmojis();
        return ResponseEntity.ok(
                ApiResponse.<List<ItemEmojiInfoResponse>>builder()
                        .result(emojis)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build()
        );
    }



}
