package com.swyp.artego.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.swyp.artego.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentFindByItemIdResponse {

    // TODO: 중복 정보(사용자 id, name, imgUrl 등)는 meta로 한번만 포함한다.
    private Long id;
    private String writer;
    private String comment;
    private boolean secret;
    // TODO: private boolean canSee;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CommentReplyFindByItemIdResponse> reply;

    public static CommentFindByItemIdResponse fromEntity(Comment comment) {
        return CommentFindByItemIdResponse.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .writer(comment.getUser().getName())
                .createdAt(comment.getCreatedAt())
                .secret(comment.isSecret())
                .build();
    }

}
