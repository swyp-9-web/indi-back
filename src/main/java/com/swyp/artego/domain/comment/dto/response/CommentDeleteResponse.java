package com.swyp.artego.domain.comment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentDeleteResponse {

    private Long commentId;

    public static CommentDeleteResponse from(Long commentId){
        return CommentDeleteResponse.builder()
                .commentId(commentId)
                .build();
    }
}
