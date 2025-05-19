package com.swyp.artego.domain.comment.dto.response;

import com.swyp.artego.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentUpdateResponse {

    private Long commentId;

    public static CommentUpdateResponse fromEntity(Comment comment){
        return CommentUpdateResponse.builder()
                .commentId(comment.getId())
                .build();
    }
}
