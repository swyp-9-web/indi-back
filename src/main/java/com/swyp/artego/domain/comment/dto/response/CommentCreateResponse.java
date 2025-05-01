package com.swyp.artego.domain.comment.dto.response;

import com.swyp.artego.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentCreateResponse {

    private Long commentId;

    public static CommentCreateResponse fromEntity(Comment comment){
        return CommentCreateResponse.builder()
                .commentId(comment.getId())
                .build();
    }
}
