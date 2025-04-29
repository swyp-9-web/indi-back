package com.swyp.artego.domain.item.controller;

import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;
import com.swyp.artego.domain.item.dto.response.ItemCreateResponse;
import com.swyp.artego.domain.item.dto.response.ItemInfoResponse;
import com.swyp.artego.domain.item.service.ItemService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Tag(name = "items", description = "작품 API")
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final Validator validator;

    @Value("${ncp.storage.bucket.folder.item-post}")
    private String folderName;

    /**
     * 작품 생성 API
     */
    @PostMapping(value = "/",
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

        ItemCreateResponse res = itemService.createItem(user, request, multipartFiles, folderName);

        ApiResponse ar = ApiResponse.builder()
                .result(res)
                .resultCode(SuccessCode.INSERT_SUCCESS.getStatus())
                .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                .build();

        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

    /**
     * 아이템 전체 조회 API (최신순)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemInfoResponse>>> getAllItems() {
        List<ItemInfoResponse> items = itemService.getAllItems();

        return ResponseEntity.ok(
                ApiResponse.<List<ItemInfoResponse>>builder()
                        .result(items)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build()
        );
    }


}
