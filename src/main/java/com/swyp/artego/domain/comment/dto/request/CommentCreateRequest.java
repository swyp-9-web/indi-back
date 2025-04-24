package com.swyp.artego.domain.comment.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentCreateRequest {

    private Long itemId;
    private String comment;
    private boolean isSecret;

}
