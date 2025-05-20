package com.swyp.artego.domain.artistApply.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ArtistApplyCreateRequest {

    @NotBlank
    @Size(max = 3000)
    private String artistAboutMe;

    @Email
    @NotBlank
    @Size(max = 50)
    private String email;

    @NotBlank
    @Size(max = 50)
    private String snsLink;
}