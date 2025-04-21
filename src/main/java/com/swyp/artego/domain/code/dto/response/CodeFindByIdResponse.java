package com.swyp.artego.domain.code.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class CodeFindByIdResponse {

    Long id;
    String grpCd;
    String cd;
    String grpCdNm;
    String cdNm;
}
