package com.swyp.artego.domain.follow.dto.response;

import com.swyp.artego.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowPreviewResponse {

    private Long id;
    private String profileImgUrl;
    private String nickname;

    public static FollowPreviewResponse fromEntity(User artist) {
        return FollowPreviewResponse.builder()
                .id(artist.getId())
                .profileImgUrl(artist.getImgUrl())
                .nickname(artist.getName()) // or artist.getNickname() if nickname exists
                .build();
    }
}