package com.swyp.artego.domain.artistApply.dto.response;

import com.swyp.artego.domain.artistApply.entity.ArtistApply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ArtistApplyFindAllResponse {

    private Long id;
    private String email;
    private String snsLink;
    private String artistAboutMe;
    private String status;
    private int rejectedCount;
    private LocalDateTime createdAt;

    private UserInfo user;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String nickname;
    }


    public static ArtistApplyFindAllResponse fromEntity(ArtistApply artistApply) {
        return ArtistApplyFindAllResponse.builder()
                .id(artistApply.getId())
                .email(artistApply.getEmail())
                .snsLink(artistApply.getSnsLink())
                .artistAboutMe(artistApply.getArtistAboutMe())
                .status(artistApply.getStatus().name())
                .rejectedCount(artistApply.getRejectedCount())
                .createdAt(artistApply.getCreatedAt())
                .user(
                        UserInfo.builder()
                                .id(artistApply.getUser().getId())
                                .name(artistApply.getUser().getName())
                                .nickname(artistApply.getUser().getNickname())
                                .build()
                )
                .build();
    }
}
