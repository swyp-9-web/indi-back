package com.swyp.artego.domain.comment.controller;

import com.swyp.artego.domain.comment.dto.request.CommentCreateRequest;
import com.swyp.artego.domain.comment.dto.response.CommentCreateResponse;
import com.swyp.artego.domain.comment.dto.response.CommentFindByItemIdResponse;
import com.swyp.artego.domain.comment.dto.response.CommentInfoResponse;
import com.swyp.artego.domain.comment.service.CommentService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
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
            @RequestBody CommentCreateRequest request) {

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
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<List<CommentFindByItemIdResponse>>> getCommentsByItemId(
            @PathVariable Long itemId) {

        List<CommentFindByItemIdResponse> res = commentService.getCommentsByItemId(itemId);

        return ResponseEntity.status(SuccessCode.SELECT_SUCCESS.getStatus())
                .body(ApiResponse.<List<CommentFindByItemIdResponse>>builder()
                        .result(res)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
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
