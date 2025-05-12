package com.swyp.artego.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.swyp.artego.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Builder
public class CommentFindByItemIdResponse {

    private Long rootCommentId;

    private Long id;
    private String content;
    private Boolean isSecret;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserInfo user;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CommentInfo> replies;


    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String profileImgUrl;
        private Boolean isOwner;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class CommentInfo {
        private Long rootCommentId;

        private Long id;
        private String content;
        private Boolean isSecret;
        private Boolean isDeleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private UserInfo user;
    }

    /**
     * flat 구조의 댓글 목록을 depth 1의 계층형 댓글 응답 구조로 변환한다.
     * <p>
     * parent가 null인 댓글을 루트 댓글로 간주하고,
     * 해당 댓글을 기준으로 직접적인 자식(대댓글)들을 묶어 replies 필드에 포함한다.
     * replies는 createdAt 오래된 순으로 정렬한다.
     * 대댓글은 루트 댓글 하나에만 속하며, 추가 중첩은 지원하지 않는다.
     *
     * @param flatList 계층 구조 없이 정렬된 Comment 엔티티 리스트
     * @return List<CommentFindByItemIdResponse> 루트 댓글과 그에 속한 대댓글을 포함하는 응답 DTO 리스트
     */
    public static List<CommentFindByItemIdResponse> convertFlatToDepth1Tree(List<Comment> flatList, Long itemOwnerId) {
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

            List<CommentInfo> replyResponses = replies.stream()
                    .map(reply -> CommentInfo.builder()
                            .rootCommentId(reply.getParent().getId())
                            .id(reply.getId())
                            .content(reply.getComment())
                            .isSecret(reply.isSecret())
                            .isDeleted(reply.isDeleted())
                            .createdAt(reply.getCreatedAt())
                            .updatedAt(reply.getUpdatedAt())
                            .user(UserInfo.builder()
                                    .id(reply.getUser().getId())
                                    .nickname(reply.getUser().getName())
                                    .profileImgUrl(reply.getUser().getImgUrl())
                                    .isOwner(Objects.equals(reply.getUser().getId(), itemOwnerId))
                                    .build()
                            )
                            .build())
                    .toList();

            CommentFindByItemIdResponse response = CommentFindByItemIdResponse.builder()
                    .rootCommentId(parent.getId())
                    .id(parent.getId())
                    .content(parent.getComment())
                    .isSecret(parent.isSecret())
                    .isDeleted(parent.isDeleted())
                    .createdAt(parent.getCreatedAt())
                    .updatedAt(parent.getUpdatedAt())
                    .user(UserInfo.builder()
                            .id(parent.getUser().getId())
                            .nickname(parent.getUser().getName())
                            .profileImgUrl(parent.getUser().getImgUrl())
                            .isOwner(Objects.equals(parent.getUser().getId(), itemOwnerId))
                            .build()
                    )
                    .replies(replyResponses.isEmpty() ? null : replyResponses)
                    .build();

            responseList.add(response);
        }
        return responseList;
    }
}
