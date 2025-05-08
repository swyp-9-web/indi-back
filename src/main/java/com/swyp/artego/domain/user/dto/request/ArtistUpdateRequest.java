package com.swyp.artego.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ArtistUpdateRequest {
    private String nickname;
    private String aboutMe = "";
    private String homeLink = "";
    private List<String> snsLinks = new ArrayList<>();
}
