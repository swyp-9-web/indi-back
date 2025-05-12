package com.swyp.artego.domain.comment.dto.response;

import com.swyp.artego.domain.comment.entity.Comment;
import com.swyp.artego.global.common.dto.response.MetaResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommentFindByItemIdWrapperResponse {
    private int totalComments;
    private List<CommentFindByItemIdResponse> comments;
    private MetaResponse meta;

    public static CommentFindByItemIdWrapperResponse from(Long itemOwnerId, List<Comment> comments, long totalComments, MetaResponse meta) {
        return CommentFindByItemIdWrapperResponse.builder()
                .totalComments((int) totalComments)
                .comments(CommentFindByItemIdResponse.convertFlatToDepth1Tree(comments, itemOwnerId))
                .meta(meta)
                .build();
    }
}
