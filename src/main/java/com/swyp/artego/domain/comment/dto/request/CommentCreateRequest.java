package com.swyp.artego.domain.comment.dto.request;

import com.swyp.artego.domain.comment.entity.Comment;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Schema(description = "댓글/대댓글 등록 정보")
public class CommentCreateRequest {

    @NotNull(message = "itemId는 필수입니다.")
    private Long itemId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String comment;

    @Schema(example = "false", nullable = true)
    private Boolean secret;

    private Long parentCommentId;

    public Comment toEntity(User user, Item item, Comment parentComment){
        return Comment.builder()
                .user(user)
                .item(item)
                .comment(this.comment)
                .secret(this.secret != null ? secret : false)
                .parent(parentComment)
                .build();
    }

}
