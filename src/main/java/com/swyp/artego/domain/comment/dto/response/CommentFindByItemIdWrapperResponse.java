package com.swyp.artego.domain.comment.dto.response;

import com.swyp.artego.domain.comment.entity.Comment;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.global.common.dto.response.MetaResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommentFindByItemIdWrapperResponse {
    private int totalComments;
    private CommentFindByItemIdResponse.UserInfo creator;
    private List<CommentFindByItemIdResponse> comments;
    private MetaResponse meta;

    public static CommentFindByItemIdWrapperResponse from(User creator, List<Comment> comments, long totalComments, MetaResponse meta) {
        return CommentFindByItemIdWrapperResponse.builder()
                .totalComments((int) totalComments)
                .creator(CommentFindByItemIdResponse.UserInfo.builder()
                        .id(creator.getId())
                        .name(creator.getName())
                        .imgUrl(creator.getImgUrl())
                        .build())
                .comments(CommentFindByItemIdResponse.convertFlatToDepth1Tree(comments))
                .meta(meta)
                .build();
    }
}
