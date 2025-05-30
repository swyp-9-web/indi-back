package com.swyp.artego.domain.user.controller;


import com.swyp.artego.domain.user.dto.request.ArtistUpdateRequest;
import com.swyp.artego.domain.user.dto.request.UserUpdateRequest;
import com.swyp.artego.domain.user.dto.response.ArtistDetailInfoResponse;
import com.swyp.artego.domain.user.dto.response.UserInfoSimpleResponse;
import com.swyp.artego.domain.user.service.UserService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "User", description = "유저 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "내 유저 정보 조회", description = "로그인한 유저의 정보를 반환합니다.")
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

    @Operation(summary = "닉네임 중복 여부 확인", description = "입력한 닉네임이 이미 사용 중인지 여부를 확인합니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNicknameDuplicate(
            @RequestParam String nickname
    ) {
        boolean isDuplicated = userService.isNicknameDuplicated(nickname);

        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .result(isDuplicated)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build()
        );
    }


    @Operation(summary = "작가 프로필 수정", description = "닉네임, 소개, 프로필 이미지 등을 수정합니다.")
    @PatchMapping(value = "/profile/artist", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ArtistDetailInfoResponse>> updateArtistProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart ArtistUpdateRequest request,
            @RequestPart(required = false) MultipartFile profileImage
    ) {
        ArtistDetailInfoResponse response = userService.updateArtistProfile(authUser, request, profileImage);

        return ResponseEntity.ok(
                ApiResponse.<ArtistDetailInfoResponse>builder()
                        .result(response)
                        .resultCode(Integer.parseInt(SuccessCode.UPDATE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.UPDATE_SUCCESS.getMessage())
                        .build()
        );
    }

    @Operation(summary = "일반 유저 프로필 수정", description = "닉네임, 프로필 이미지 수정")
    @PatchMapping(value = "/profile/user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserInfoSimpleResponse>> updateUserProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart UserUpdateRequest request,
            @RequestPart(required = false) MultipartFile profileImage
    ) {
        UserInfoSimpleResponse response = userService.updateUserProfile(authUser, request, profileImage);

        return ResponseEntity.ok(
                ApiResponse.<UserInfoSimpleResponse>builder()
                        .result(response)
                        .resultCode(Integer.parseInt(SuccessCode.UPDATE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.UPDATE_SUCCESS.getMessage())
                        .build()
        );
    }

}
