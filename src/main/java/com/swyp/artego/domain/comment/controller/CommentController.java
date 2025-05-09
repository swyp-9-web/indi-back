package com.swyp.artego.domain.comment.controller;

import com.swyp.artego.domain.comment.dto.request.CommentCreateRequest;
import com.swyp.artego.domain.comment.dto.request.CommentUpdateRequest;
import com.swyp.artego.domain.comment.dto.response.*;
import com.swyp.artego.domain.comment.service.CommentService;
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

@Tag(name = "Comment", description = "댓글/대댓글 API")
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글/대댓글 생성 API
     */
    @PostMapping("")
    @Operation(summary = "댓글/대댓글 등록")
    public ResponseEntity<ApiResponse<CommentCreateResponse>> createComment(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody @Valid CommentCreateRequest request) {

        CommentCreateResponse res = commentService.createComment(user, request);

        return ResponseEntity.status(SuccessCode.INSERT_SUCCESS.getStatus())
                .body(ApiResponse.<CommentCreateResponse>builder()
                        .result(res)
                        .resultCode(Integer.parseInt(SuccessCode.INSERT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                        .build());
    }

    /**
     * 작품 별 전체 댓글 조회 API
     * TODO: 프런트 연동 이후 @AuthenticationPrincipal AuthUser user 를 추가, 볼 수 있는/없는 댓글을 응답에 적용한다.
     */
    @GetMapping("/item/{itemId}")
    @Operation(summary = "작품 별 댓글/대댓글 전체 조회")
    public ResponseEntity<ApiResponse<CommentFindByItemIdWrapperResponse>> getCommentsByItemId(
            @PathVariable Long itemId) {

        CommentFindByItemIdWrapperResponse res = commentService.getCommentsByItemId(itemId);

        return ResponseEntity.status(SuccessCode.SELECT_SUCCESS.getStatus())
                .body(ApiResponse.<CommentFindByItemIdWrapperResponse>builder()
                        .result(res)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build());
    }

    @GetMapping("/my-activities")
    @Operation(summary = "내가 작성한 댓글 기반 활동 목록 조회 (작품별 최신 댓글 + 작가 대댓글 포함)")
    public ResponseEntity<ApiResponse<MyCommentActivityResultResponse>> getMyCommentActivities(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        MyCommentActivityResultResponse result = commentService.getMyCommentActivities(user, page, limit);

        return ResponseEntity.status(SuccessCode.SELECT_SUCCESS.getStatus())
                .body(ApiResponse.<MyCommentActivityResultResponse>builder()
                        .result(result)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build());
    }


    /**
     * 댓글/대댓글 수정 API
     */
    @PatchMapping("/{commentId}")
    @Operation(summary = "댓글/대댓글 수정")
    public ResponseEntity<ApiResponse<CommentUpdateResponse>> updateComment(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest request) {

        CommentUpdateResponse res = commentService.updateComment(user, commentId, request);

        return ResponseEntity.status(SuccessCode.UPDATE_SUCCESS.getStatus())
                .body(ApiResponse.<CommentUpdateResponse>builder()
                        .result(res)
                        .resultCode(Integer.parseInt(SuccessCode.UPDATE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.UPDATE_SUCCESS.getMessage())
                        .build());
    }

    /**
     * 댓글/대댓글 삭제 API
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글/대댓글 삭제")
    public ResponseEntity<ApiResponse<CommentDeleteResponse>> deleteComment(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long commentId) {

        CommentDeleteResponse res = commentService.deleteComment(user, commentId);

        return ResponseEntity.status(SuccessCode.DELETE_SUCCESS.getStatus())
                .body(ApiResponse.<CommentDeleteResponse>builder()
                        .result(res)
                        .resultCode(Integer.parseInt(SuccessCode.DELETE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.DELETE_SUCCESS.getMessage())
                        .build());
    }
}
