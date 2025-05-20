package com.swyp.artego.domain.artistApply.dto.response;


import com.swyp.artego.domain.artistApply.entity.ArtistApply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ArtistApplyCreateResponse {
    private Long id;

    /**
     * ArtistApply 엔티티 -> DTO 변환 메서드
     */
    public static ArtistApplyCreateResponse fromEntity(ArtistApply artistApply) {
        return ArtistApplyCreateResponse.builder()
                .id(artistApply.getId())
                .build();
    }
}
