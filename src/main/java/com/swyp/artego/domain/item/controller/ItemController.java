package com.swyp.artego.domain.item.controller;

import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;
import com.swyp.artego.domain.item.dto.request.ItemSearchRequest;
import com.swyp.artego.domain.item.dto.request.ItemUpdateRequest;
import com.swyp.artego.domain.item.dto.response.*;
import com.swyp.artego.domain.item.service.ItemService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Tag(name = "Item", description = "작품 API")
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final Validator validator;

    /**
     * 작품 생성 API
     */
    @PostMapping(value = "",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "작품 등록", description = "작품을 등록하는 메소드입니다.")
    public ResponseEntity<ApiResponse> createItem(
            @AuthenticationPrincipal AuthUser user,
            @RequestPart("request") ItemCreateRequest request,

            @Schema(description = "이미지 정보")
            @Size(min = 1, max = 8, message = "이미지는 1개 이상 8개 이하만 업로드 가능합니다.")
            @RequestPart(value = "images") List<MultipartFile> multipartFiles
    ) {
        /*
        Request DTO 수동 검증
        multipart/form-data + JSON 혼용 시 @RequestPart("request")에 @Valid가 자동 적용되지 않는 문제를 해결하기 위함
         */
        Set<ConstraintViolation<ItemCreateRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        ItemCreateResponse res = itemService.createItem(user, request, multipartFiles);

        ApiResponse ar = ApiResponse.builder()
                .result(res)
                .resultCode(SuccessCode.INSERT_SUCCESS.getStatus())
                .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                .build();

        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

    /**
     * 작품 세부조회 API
     */
    @GetMapping(value = "/{itemId}")
    @Operation(summary = "작품 세부조회")
    public ResponseEntity<ApiResponse<ItemFindByItemIdResponse>> findItemByItemId(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long itemId) {

        ItemFindByItemIdResponse res = itemService.findItemByItemId(user, itemId);

        return ResponseEntity.status(SuccessCode.SELECT_SUCCESS.getStatus())
                .body(ApiResponse.<ItemFindByItemIdResponse>builder()
                        .result(res)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build());
    }

    /**
     * 작품 수정 API
     */
    @PatchMapping(value = "/{itemId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "작품 수정")
    public ResponseEntity<ApiResponse<ItemUpdateResponse>> updateItem(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long itemId,
            @RequestPart("request") ItemUpdateRequest request,

            @Schema(description = "이미지 정보")
            @Size(max = 8, message = "8개 이하만 업로드 가능합니다.")
            @RequestPart(value = "images", required = false) List<MultipartFile> multipartFiles
    ) {
        /*
        Request DTO 수동 검증
        multipart/form-data + JSON 혼용 시 @RequestPart("request")에 @Valid가 자동 적용되지 않는 문제를 해결하기 위함
         */
        Set<ConstraintViolation<ItemUpdateRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        ItemUpdateResponse res = itemService.updateItem(user, itemId, request, multipartFiles);

        return ResponseEntity.status(SuccessCode.UPDATE_SUCCESS.getStatus())
                .body(ApiResponse.<ItemUpdateResponse>builder()
                        .result(res)
                        .resultCode(Integer.parseInt(SuccessCode.UPDATE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.UPDATE_SUCCESS.getMessage())
                        .build());
    }

    /**
     * 작품 삭제 API
     */
    @DeleteMapping(value = "/{itemId}")
    @Operation(summary = "작품 삭제", description = "작품을 삭제하는 메소드입니다. 참고: 실제 DB에서 삭제하는 건 아님")
    public ResponseEntity<ApiResponse<ItemDeleteResponse>> deleteItem(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long itemId) {

        ItemDeleteResponse res = itemService.deleteItem(user, itemId);

        return ResponseEntity.status(SuccessCode.DELETE_SUCCESS.getStatus())
                .body(ApiResponse.<ItemDeleteResponse>builder()
                        .result(res)
                        .resultCode(Integer.parseInt(SuccessCode.DELETE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.DELETE_SUCCESS.getMessage())
                        .build());
    }


    /**
     * 아이템 검색 API
     */

    @Operation(
            summary = "작품 검색",
            description = "검색 조건에 맞는 작품을 페이지네이션 형태로 조회합니다.\n\n" +
                    "- 작가 이름 또는 작품명으로 검색 가능\n" +
                    "- 카테고리, 사이즈 필터링 가능\n" +
                    "- 정렬 조건 (최신순, 인기순 등)\n" +
                    "- 특정 작가의 작품만 조회도 가능"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ItemSearchResultResponse>> searchItems(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal AuthUser user,
            @ParameterObject @ModelAttribute ItemSearchRequest request) {

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
