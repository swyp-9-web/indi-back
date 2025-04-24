package com.swyp.artego.domain.scrap.controller;

import com.swyp.artego.domain.scrap.dto.response.ScrapInfoResponse;
import com.swyp.artego.domain.scrap.service.ScrapService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scraps")
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;

    /**
     * 스크랩 생성 API
     */
    @PostMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> createScrap(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long itemId) {

        scrapService.createScrap(user, itemId);
        return ResponseEntity.status(SuccessCode.INSERT_SUCCESS.getStatus())
                .body(ApiResponse.<Void>builder()
                        .result(null)
                        .resultCode(Integer.parseInt(SuccessCode.INSERT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                        .build());
    }

    /**
     * 스크랩 전체 조회 API
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScrapInfoResponse>>> getAllScraps() {
        List<ScrapInfoResponse> scraps = scrapService.getAllScraps();
        return ResponseEntity.ok(
                ApiResponse.<List<ScrapInfoResponse>>builder()
                        .result(scraps)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build()
        );
    }

}
