package com.swyp.artego.domain.itemEmoji.controller;

import com.swyp.artego.domain.itemEmoji.dto.request.ItemEmojiCreateRequest;
import com.swyp.artego.domain.itemEmoji.dto.response.ItemEmojiInfoResponse;
import com.swyp.artego.domain.itemEmoji.service.ItemEmojiService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "아이템 이모지 생성", description = "사용자가 특정 아이템에 이모지를 추가합니다. 동일한 이모지는 중복 등록할 수 없습니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createItemEmoji(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user,
            @RequestBody ItemEmojiCreateRequest request) {

        Long emojiId = itemEmojiService.createItemEmoji(user, request);

        return ResponseEntity.status(SuccessCode.INSERT_SUCCESS.getStatus())
                .body(ApiResponse.<Long>builder()
                        .result(emojiId)
                        .resultCode(Integer.parseInt(SuccessCode.INSERT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                        .build());
    }



    /**
     * 이모지 삭제 API
     */
    @Operation(summary = "이모지 삭제", description = "이모지의 PK(ID)를 기반으로 해당 이모지를 삭제합니다.")
    @DeleteMapping("/{itemEmojiId}")
    public ResponseEntity<ApiResponse<Void>> deleteItemEmojiById(
            @Parameter(description = "삭제할 이모지 ID", example = "123") @PathVariable Long itemEmojiId) {

        itemEmojiService.deleteItemEmojiById(itemEmojiId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .result(null)
                        .resultCode(Integer.parseInt(SuccessCode.DELETE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.DELETE_SUCCESS.getMessage())
                        .build()
        );
    }
}
