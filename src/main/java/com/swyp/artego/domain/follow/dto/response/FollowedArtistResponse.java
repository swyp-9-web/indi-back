package com.swyp.artego.domain.follow.dto.response;

import com.swyp.artego.domain.item.dto.response.ItemSearchResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder // 팔로우 전체 목록 조회에서 쓰이는 DTO
public class FollowedArtistResponse {

    private Long id;
    private String profileImgUrl;
    private String nickname;
    private int totalItems;
    private int totalFollower;
    private Boolean isFollowing;
    private List<ItemSearchResponse> items;
}
