package com.swyp.artego.domain.code.service;

import com.swyp.artego.domain.code.dto.request.*;
import com.swyp.artego.domain.code.dto.response.*;

import java.util.List;

public interface CodeService {

    /**
     * Code 생성
     *
     * @param codeDto 코드
     */
    CodeCreateResponse createCode(CodeCreateRequest codeDto);

    /**
     * 전체 Code 조회
     */
    List<CodeFindAllResponse> findAllCode();

    /**
     * Code 단건 조회, 검색 조건: id
     */
    CodeFindByIdResponse findCodeById(Long id);
}
