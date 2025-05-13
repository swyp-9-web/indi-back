package com.swyp.artego.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyCommentActivityResponse {

    private ItemDTO item;

    private CommentDTO myComment;

    private CommentDTO replyComment;

    private int totalReplies;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemDTO {
        private Long id;
        private String title;
        private int price;
        private ArtistDTO artist;
        private String thumbnailImgUrl;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ArtistDTO {
            private Long id;
            private String nickname;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentDTO {
        private Long id;
        private String content;
        private LocalDateTime createdAt;
        private UserDTO user;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDTO {
        private Long id;
        private String nickname;
        private String profileImgUrl;
    }
}
