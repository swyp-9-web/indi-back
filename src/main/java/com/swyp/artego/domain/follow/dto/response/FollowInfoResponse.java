package com.swyp.artego.domain.follow.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swyp.artego.domain.follow.entity.Follow;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FollowInfoResponse {

    private Long followId;
    private String followerName;
    private String artistName;

    private LocalDateTime createdAt;


    public static FollowInfoResponse fromEntity(Follow follow) {
        return FollowInfoResponse.builder()
                .followId(follow.getId())
                .followerName(follow.getUser().getName())
                .artistName(follow.getUserArtist().getName())
                .createdAt(follow.getCreatedAt())
                .build();
    }
}
