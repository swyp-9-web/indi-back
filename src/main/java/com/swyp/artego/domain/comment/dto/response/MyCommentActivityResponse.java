package com.swyp.artego.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyCommentActivityResponse {

    private Long itemId;
    private String itemTitle;
    private int itemPrice;

    @JsonIgnore // 직렬화 대상에서 제외
    private List<String> itemImgUrls;
    private String artistNickname;

    private CommentDto myComment;
    private CommentDto artistReply;

    @JsonProperty("itemThumbnailUrl") // 프론트에는 이 필드로 노출됨
    public String getItemThumbnailUrl() {
        return itemImgUrls != null && !itemImgUrls.isEmpty() ? itemImgUrls.get(0) : null;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentDto {
        private Long commentId;
        private String content;
        private LocalDateTime createdAt;
        private String nickname;
        private String profileImgUrl;
    }
}
