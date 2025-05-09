package com.swyp.artego.domain.comment.service;

import com.swyp.artego.domain.comment.dto.request.CommentCreateRequest;
import com.swyp.artego.domain.comment.dto.request.CommentUpdateRequest;
import com.swyp.artego.domain.comment.dto.response.CommentCreateResponse;
import com.swyp.artego.domain.comment.dto.response.CommentDeleteResponse;
import com.swyp.artego.domain.comment.dto.response.CommentFindByItemIdWrapperResponse;
import com.swyp.artego.domain.comment.dto.response.CommentUpdateResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

public interface CommentService {

    /**
     * 댓글/대댓글 작성
     *
     * @param user    댓글/대댓글을 작성하는 유저
     * @param request
     * @return CommentCreateResponse
     */
    CommentCreateResponse createComment(AuthUser user, CommentCreateRequest request);

    /**
     * 작품 별 댓글 전체 조회 (최신순)
     *
     * @param itemId 댓글을 조회할 작품 Id
     * @return CommentFindByItemIdWrapperResponse
     */
    CommentFindByItemIdWrapperResponse getCommentsByItemId(Long itemId);

    /**
     * 댓글/대댓글 수정
     *
     * @param user      댓글 수정을 시도하는 유저
     * @param commentId 수정하려는 댓글의 id
     * @return CommentUpdateResponse
     */
    CommentUpdateResponse updateComment(AuthUser user, Long commentId, CommentUpdateRequest request);

    /**
     * 댓글/대댓글 삭제
     *
     * @param user      댓글 삭제를 시도하는 유저
     * @param commentId 삭제하려는 댓글의 id
     * @return CommentDeleteResponse
     */
    CommentDeleteResponse deleteComment(AuthUser user, Long commentId);
}
