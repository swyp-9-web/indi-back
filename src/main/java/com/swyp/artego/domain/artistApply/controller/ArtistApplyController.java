package com.swyp.artego.domain.artistApply.controller;

import com.swyp.artego.domain.artistApply.dto.request.ArtistApplyCreateRequest;
import com.swyp.artego.domain.artistApply.dto.request.ConvertToArtistRequest;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyCreateResponse;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyFindAllResponse;
import com.swyp.artego.domain.artistApply.service.ArtistApplyService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ArtistApply", description = "작가 신청 API")
@RestController
@RequestMapping("/api/v1/artist-applies")
@RequiredArgsConstructor
public class ArtistApplyController {

    private final ArtistApplyService artistApplyService;

    @PostMapping
    @Operation(summary = "작가 신청 등록")
    public ResponseEntity<ApiResponse<ArtistApplyCreateResponse>> createArtistApply(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid ArtistApplyCreateRequest request) {

        ArtistApplyCreateResponse res = artistApplyService.createArtistApply(authUser, request);

        return ResponseEntity.status(SuccessCode.INSERT_SUCCESS.getStatus())
                .body(ApiResponse.<ArtistApplyCreateResponse>builder()
                        .result(res)
                        .resultCode(Integer.parseInt(SuccessCode.INSERT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                        .build());
    }

    @GetMapping("/admin")
    @Operation(summary = "작가 신청 전체 조회")
    public ResponseEntity<ApiResponse<List<ArtistApplyFindAllResponse>>> getArtistApplies() {

        List<ArtistApplyFindAllResponse> res = artistApplyService.getArtistApplies();

        return ResponseEntity.status(SuccessCode.SELECT_SUCCESS.getStatus())
                .body(ApiResponse.<List<ArtistApplyFindAllResponse>>builder()
                        .result(res)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build());
    }


    @PostMapping("/grant-artist-role")
    @Operation(summary = "어드민이 유저에게 ARTIST 권한 부여")
    public ResponseEntity<ApiResponse<String>> grantArtistRole(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid ConvertToArtistRequest request) {

        artistApplyService.convertUserToArtist(authUser, request);

        return ResponseEntity.status(SuccessCode.UPDATE_SUCCESS.getStatus())
                .body(ApiResponse.<String>builder()
                        .result("작가 권한이 부여되었습니다.")
                        .resultCode(Integer.parseInt(SuccessCode.UPDATE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.UPDATE_SUCCESS.getMessage())
                        .build());
    }


}