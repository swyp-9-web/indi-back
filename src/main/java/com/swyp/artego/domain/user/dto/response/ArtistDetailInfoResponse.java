package com.swyp.artego.domain.user.dto.response;

import com.swyp.artego.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ArtistDetailInfoResponse {

    private Long id;
    private String profileImgUrl;
    private String nickname;
    private String aboutMe;
    private Boolean isFollowing;
    private int totalItems;
    private int totalScraps;
    private int totalReactions;
    private int totalFollower;
    private String homeLink;
    private List<String> snsLinks;

    public static ArtistDetailInfoResponse from(User artist, Boolean isFollowing) {
        return ArtistDetailInfoResponse.builder()
                .id(artist.getId())
                .profileImgUrl(artist.getImgUrl())
                .nickname(artist.getNickname())
                .aboutMe(artist.getArtistAboutMe())
                .isFollowing(isFollowing)
                .totalItems(artist.getItemCount())
                .totalScraps(artist.getScrapCount())
                .totalReactions(artist.getReactionCount())
                .totalFollower(artist.getFollowerCount())
                .homeLink(artist.getArtistHomeSnsInfo())
                .snsLinks(artist.getArtistSnsInfo())
                .build();
    }

}
