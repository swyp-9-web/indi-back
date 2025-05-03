package com.swyp.artego.domain.comment.controller;

import com.swyp.artego.domain.comment.dto.request.CommentCreateRequest;
import com.swyp.artego.domain.comment.dto.request.CommentUpdateRequest;
import com.swyp.artego.domain.comment.dto.response.CommentUpdateResponse;
import com.swyp.artego.domain.comment.dto.response.*;
import com.swyp.artego.domain.comment.service.CommentService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글/대댓글 생성 API
     */
    @PostMapping
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
     * 게시글 별 전체 댓글 조회 API
     * TODO: 프런트 연동 이후 @AuthenticationPrincipal AuthUser user 를 추가, 볼 수 있는/없는 댓글을 응답에 적용한다.
     */
    @GetMapping("/{itemId}")
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

    /**
     * 댓글/대댓글 수정 API
     * TODO: 기획- 답글이 달린 경우, 수정 불가능하게?
     */
    @PatchMapping("/update/{commentId}")
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
    @DeleteMapping("/delete/{commentId}")
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

    /**
     * 댓글 전체 조회 API (최신순)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentInfoResponse>>> getAllComments() {
        List<CommentInfoResponse> comments = commentService.getAllComments();

        return ResponseEntity.status(SuccessCode.SELECT_SUCCESS.getStatus())
                .body(ApiResponse.<List<CommentInfoResponse>>builder()
                        .result(comments)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build());
    }


}
