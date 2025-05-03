package com.swyp.artego.domain.comment.dto.request;

import com.swyp.artego.global.common.annotation.NullableNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentUpdateRequest {

    @NullableNotBlank
    @Schema(example = "댓글 내용", nullable = true)
    private String comment;

    @Schema(example = "false", nullable = true)
    private Boolean secret;

}
