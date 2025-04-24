package com.swyp.artego.domain.comment.service;

import com.swyp.artego.domain.comment.dto.request.CommentCreateRequest;
import com.swyp.artego.domain.comment.dto.response.CommentInfoResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

import java.util.List;

public interface CommentService {

    /**
     * 댓글 생성
     */
    void createComment(AuthUser user, CommentCreateRequest request);

    /**
     * 댓글 전체 조회 (최신순)
     */
    List<CommentInfoResponse> getAllComments();
}
