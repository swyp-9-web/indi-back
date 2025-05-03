package com.swyp.artego.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.swyp.artego.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Builder
public class CommentFindByItemIdResponse {

    private UserInfo user;
    private ParentComment comment;
    // TODO: private boolean canSee;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CommentReply> replies;


    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String imgUrl;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BaseComment {
        private Long writerId;
        private String comment;
        private boolean secret;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
    }

    @JsonPropertyOrder({"id", "writerId", "comment", "secret", "createdAt"})
    @Getter
    @NoArgsConstructor
    public static class ParentComment extends BaseComment {
        private Long id;

        @Builder(builderMethodName = "parentCommentBuilder")
        public ParentComment(Long id, Long writerId, String comment, boolean secret, LocalDateTime createdAt) {
            super(writerId, comment, secret, createdAt);
            this.id = id;
        }
    }

    @Getter
    public static class CommentReply extends BaseComment {
        @Builder(builderMethodName = "replyBuilder")
        public CommentReply(Long writerId, String comment, boolean secret, LocalDateTime createdAt) {
            super(writerId, comment, secret, createdAt);
        }
    }

    /**
     * flat 구조의 댓글 목록을 depth 1의 계층형 댓글 응답 구조로 변환한다.
     *
     * parent가 null인 댓글을 루트 댓글로 간주하고,
     * 해당 댓글을 기준으로 직접적인 자식(대댓글)들을 묶어 replies 필드에 포함한다.
     * 대댓글은 루트 댓글 하나에만 속하며, 추가 중첩은 지원하지 않는다.
     *
     * @param flatList  계층 구조 없이 정렬된 Comment 엔티티 리스트
     * @return List<CommentFindByItemIdResponse> 루트 댓글과 그에 속한 대댓글을 포함하는 응답 DTO 리스트
     */
    public static List<CommentFindByItemIdResponse> convertFlatToDepth1Tree(List<Comment> flatList) {
        Map<Long, List<Comment>> repliesGroupedByParentId = new HashMap<>();
        List<Comment> parents = new ArrayList<>();

        for (Comment comment : flatList) {
            if (comment.getParent() == null) {
                parents.add(comment);
            } else {
                Long parentId = comment.getParent().getId();
                repliesGroupedByParentId
                        .computeIfAbsent(parentId, k -> new ArrayList<>())
                        .add(comment);
            }
        }

        List<CommentFindByItemIdResponse> responseList = new ArrayList<>();

        for (Comment parent : parents) {
            List<Comment> replies = repliesGroupedByParentId.getOrDefault(parent.getId(), Collections.emptyList());

            List<CommentReply> replyResponses = replies.stream()
                    .map(reply -> CommentReply.replyBuilder()
                            .writerId(reply.getUser().getId())
                            .comment(reply.getComment())
                            .secret(reply.isSecret())
                            .createdAt(reply.getCreatedAt())
                            .build())
                    .toList();

            CommentFindByItemIdResponse response = CommentFindByItemIdResponse.builder()
                    .user(UserInfo.builder()
                            .id(parent.getUser().getId())
                            .name(parent.getUser().getName())
                            .imgUrl("user's imgUrl") // TODO: 프로필 사진 연동하기
                            .build())
                    .comment(ParentComment.parentCommentBuilder()
                            .id(parent.getId())
                            .writerId(parent.getUser().getId())
                            .comment(parent.getComment())
                            .secret(parent.isSecret())
                            .createdAt(parent.getCreatedAt())
                            .build())
                    .replies(replyResponses.isEmpty() ? null : replyResponses)
                    .build();

            responseList.add(response);
        }

        return responseList;
    }
}
