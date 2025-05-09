package com.swyp.artego.domain.comment.repository;

import com.swyp.artego.domain.comment.dto.response.MyCommentActivityResultResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

public interface CommentQueryRepository {
    MyCommentActivityResultResponse findMyCommentActivity(AuthUser authUser, int page, int limit);
}
