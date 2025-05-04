package com.swyp.artego.domain.follow.controller;

import com.swyp.artego.domain.follow.dto.response.FollowInfoResponse;
import com.swyp.artego.domain.follow.dto.response.FollowPreviewListResponse;
import com.swyp.artego.domain.follow.service.FollowService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
@Tag(name = "Follow", description = "팔로우 관련 API")
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "팔로우 생성", description = "아티스트 ID를 기반으로 팔로우를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createFollow(
            @Parameter(description = "로그인한 사용자 정보", hidden = true)
            @AuthenticationPrincipal AuthUser user,
            @Parameter(description = "팔로우할 아티스트의 ID") @PathVariable Long artistId) {

        followService.createFollow(user, artistId);

        return ResponseEntity.status(SuccessCode.INSERT_SUCCESS.getStatus())
                .body(ApiResponse.<Void>builder()
                        .result(null)
                        .resultCode(Integer.parseInt(SuccessCode.INSERT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                        .build());
    }

    @Operation(summary = "팔로우 프리뷰", description = "최근 팔로우한 아티스트 최대 5명을 반환합니다.")
    @GetMapping("/preview")
    public ResponseEntity<ApiResponse<FollowPreviewListResponse>> getFollowPreview(
            @Parameter(description = "로그인한 사용자 정보", hidden = true)
            @AuthenticationPrincipal AuthUser user) {

        FollowPreviewListResponse response = followService.getFollowPreview(user);

        return ResponseEntity.ok(
                ApiResponse.<FollowPreviewListResponse>builder()
                        .result(response)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage("작가 목록조회(프리뷰) 성공")
                        .build()
        );
    }


    @Operation(summary = "팔로우 전체 조회", description = "전체 팔로우 정보를 최신순으로 조회합니다.")
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

    @Operation(summary = "팔로우 취소", description = "아티스트 ID를 기반으로 팔로우를 취소합니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteFollow(
            @Parameter(description = "로그인한 사용자 정보", hidden = true)
            @AuthenticationPrincipal AuthUser user,
            @Parameter(description = "언팔로우할 아티스트의 ID") @PathVariable Long artistId) {

        followService.deleteFollow(user, artistId);

        return ResponseEntity.status(SuccessCode.DELETE_SUCCESS.getStatus())
                .body(ApiResponse.<Void>builder()
                        .result(null)
                        .resultCode(Integer.parseInt(SuccessCode.DELETE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.DELETE_SUCCESS.getMessage())
                        .build());
    }
}
