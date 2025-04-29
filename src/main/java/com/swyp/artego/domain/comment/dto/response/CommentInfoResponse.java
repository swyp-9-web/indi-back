package com.swyp.artego.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swyp.artego.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentInfoResponse {

    private String userName;
    private String comment;
    private boolean secret;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static CommentInfoResponse fromEntity(Comment comment) {
        return CommentInfoResponse.builder()
                .userName(comment.getUser().getName())
                .comment(comment.getComment())
                .secret(comment.isSecret())
                .createdAt(comment.getCreatedAt())
                .build();
    }

}
