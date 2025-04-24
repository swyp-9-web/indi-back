package com.swyp.artego.domain.comment.controller;

import com.swyp.artego.domain.comment.dto.request.CommentCreateRequest;
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
     * 댓글 생성 API
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createComment(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody CommentCreateRequest request) {

        commentService.createComment(user, request);

        return ResponseEntity.status(SuccessCode.INSERT_SUCCESS.getStatus())
                .body(ApiResponse.<Void>builder()
                        .result(null)
                        .resultCode(Integer.parseInt(SuccessCode.INSERT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                        .build());
    }



    /**
     * 댓글 전체 조회 API (최신순)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentInfoResponse>>> getAllComments() {
        List<CommentInfoResponse> comments = commentService.getAllComments();

        return ResponseEntity.ok(
                ApiResponse.<List<CommentInfoResponse>>builder()
                        .result(comments)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build()
        );
    }


}
