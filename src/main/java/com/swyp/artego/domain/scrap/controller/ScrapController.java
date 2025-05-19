package com.swyp.artego.domain.scrap.controller;

import com.swyp.artego.domain.scrap.service.ScrapService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scraps")
@RequiredArgsConstructor
@Tag(name = "Scrap", description = "스크랩 API")
public class ScrapController {

    private final ScrapService scrapService;

    @Operation(summary = "스크랩 생성", description = "특정 아이템을 스크랩합니다.")
    @PostMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> createScrap(
            @Parameter(description = "로그인한 사용자 정보", hidden = true)
            @AuthenticationPrincipal AuthUser user,
            @Parameter(description = "스크랩할 아이템의 ID") @PathVariable Long itemId) {

        scrapService.createScrap(user, itemId);
        return ResponseEntity.status(SuccessCode.INSERT_SUCCESS.getStatus())
                .body(ApiResponse.<Void>builder()
                        .result(null)
                        .resultCode(Integer.parseInt(SuccessCode.INSERT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                        .build());
    }

    @Operation(summary = "스크랩 취소", description = "특정 아이템의 스크랩을 취소합니다.")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteScrap(
            @Parameter(description = "로그인한 사용자 정보", hidden = true)
            @AuthenticationPrincipal AuthUser user,
            @Parameter(description = "스크랩을 취소할 아이템의 ID") @PathVariable Long itemId) {

        scrapService.deleteScrap(user, itemId);
        return ResponseEntity.status(SuccessCode.DELETE_SUCCESS.getStatus())
                .body(ApiResponse.<Void>builder()
                        .result(null)
                        .resultCode(Integer.parseInt(SuccessCode.DELETE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.DELETE_SUCCESS.getMessage())
                        .build());
    }


}
