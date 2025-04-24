package com.swyp.artego.domain.comment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentCreateRequest {

    private Long itemId;
    private String comment;

    @JsonProperty(value = "isSecret")
    private boolean isSecret;

}
