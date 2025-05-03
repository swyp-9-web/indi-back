package com.swyp.artego.domain.comment.dto.response;

import com.swyp.artego.domain.comment.entity.Comment;
import com.swyp.artego.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommentFindByItemIdWrapperResponse {
    private CommentFindByItemIdResponse.UserInfo creator;
    private List<CommentFindByItemIdResponse> comments;

    public static CommentFindByItemIdWrapperResponse from(User creator, List<Comment> flatList) {
        return CommentFindByItemIdWrapperResponse.builder()
                .creator(CommentFindByItemIdResponse.UserInfo.builder()
                        .id(creator.getId())
                        .name(creator.getName())
                        .imgUrl("creator's imgUrl") // TODO: 프로필 사진 연동하기
                        .build())
                .comments(CommentFindByItemIdResponse.convertFlatToDepth1Tree(flatList))
                .build();
    }
}
