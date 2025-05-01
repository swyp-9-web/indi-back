package com.swyp.artego.domain.comment.service;

import com.swyp.artego.domain.comment.dto.request.CommentCreateRequest;
import com.swyp.artego.domain.comment.dto.response.CommentCreateResponse;
import com.swyp.artego.domain.comment.dto.response.CommentInfoResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

import java.util.List;

public interface CommentService {

    /**
     * 댓글/대댓글 작성
     *
     * @param user 댓글/대댓글을 작성하는 유저
     * @param request
     * @return CommentCreateResponse
     */
    CommentCreateResponse createComment(AuthUser user, CommentCreateRequest request);

    /**
     * 댓글 전체 조회 (최신순)
     */
    List<CommentInfoResponse> getAllComments();
}
