package com.swyp.artego.domain.user.controller;

import com.swyp.artego.domain.user.dto.request.UserCreateRequest;
import com.swyp.artego.domain.user.dto.response.ArtistDetailInfoResponse;
import com.swyp.artego.domain.user.dto.response.UserInfoResponse;
import com.swyp.artego.domain.user.dto.response.UserInfoSimpleResponse;
import com.swyp.artego.domain.user.service.UserService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User", description = "유저 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @Operation(summary = "내 유저 정보 조회", description = "로그인한 유저의 간단한 정보를 반환합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoSimpleResponse>> getMyUserInfo(
            @AuthenticationPrincipal AuthUser authUser) {

        UserInfoSimpleResponse userInfo = userService.getMyUserInfo(authUser.getOauthId());

        return ResponseEntity.ok(
                ApiResponse.<UserInfoSimpleResponse>builder()
                        .result(userInfo)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build()
        );
    }

    @Operation(summary = "작가 상세 정보 조회", description = "작가 ID로 작가의 세부 정보를 반환합니다.")
    @GetMapping("/artists/{artistId}")
    public ResponseEntity<ApiResponse<ArtistDetailInfoResponse>> getArtistDetail(
            @PathVariable Long artistId,
            @AuthenticationPrincipal AuthUser authUser) {

        ArtistDetailInfoResponse result = userService.getArtistDetailInfo(artistId, authUser);

        return ResponseEntity.ok(
                ApiResponse.<ArtistDetailInfoResponse>builder()
                        .result(result)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build()
        );
    }



}
