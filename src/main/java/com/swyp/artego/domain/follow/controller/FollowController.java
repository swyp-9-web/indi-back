package com.swyp.artego.domain.follow.controller;

import com.swyp.artego.domain.follow.dto.response.FollowInfoResponse;
import com.swyp.artego.domain.follow.service.FollowService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 팔로우 생성 API
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createFollow(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam Long artistId) {

        followService.createFollow(user, artistId);

        return ResponseEntity.status(SuccessCode.INSERT_SUCCESS.getStatus())
                .body(ApiResponse.<Void>builder()
                        .result(null)
                        .resultCode(Integer.parseInt(SuccessCode.INSERT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                        .build());
    }

    /**
     * 팔로우 전체 조회 API (최신순)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FollowInfoResponse>>> getAllFollows() {
        List<FollowInfoResponse> follows = followService.getAllFollows();

        return ResponseEntity.ok(
                ApiResponse.<List<FollowInfoResponse>>builder()
                        .result(follows)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build()
        );
    }
}
