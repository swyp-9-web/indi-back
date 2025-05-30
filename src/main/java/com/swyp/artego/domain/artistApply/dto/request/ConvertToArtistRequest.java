package com.swyp.artego.domain.artistApply.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ConvertToArtistRequest {

    @NotNull(message = "user_id는 필수입니다.")
    private Long userId;
    @NotNull(message = "artist_apply_id는 필수입니다.")
    private Long artistApplyId;

}