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

    private Boolean isFollowing; //팔로잉 여부 => 프론트 요구에 따라 생성

    public static FollowPreviewResponse fromEntity(User artist) {
        return FollowPreviewResponse.builder()
                .id(artist.getId())
                .profileImgUrl(artist.getImgUrl())
                .nickname(artist.getNickname())
                .isFollowing(true)
                .build();
    }


}