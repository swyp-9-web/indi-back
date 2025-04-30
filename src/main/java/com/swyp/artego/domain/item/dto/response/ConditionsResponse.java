package com.swyp.artego.domain.item.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swyp.artego.domain.item.enums.SortType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConditionsResponse {

    private String search;
    private FiltersResponse filters;


    private boolean isLogin;

    private boolean isScrapedPage;


    private boolean isArtistPage;
    private SortType sortType;
}