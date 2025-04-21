package com.swyp.artego.domain.code.service;

import com.swyp.artego.domain.code.dto.request.CodeCreateRequest;
import com.swyp.artego.domain.code.dto.response.CodeCreateResponse;
import com.swyp.artego.domain.code.dto.response.CodeFindAllResponse;
import com.swyp.artego.domain.code.dto.response.CodeFindByIdResponse;
import com.swyp.artego.domain.code.entity.Code;
import com.swyp.artego.domain.code.repository.CodeRepository;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeServiceImpl implements CodeService{

    private final CodeRepository codeRepository;

    @Override
    @Transactional
    public CodeCreateResponse createCode(CodeCreateRequest codeDto) {
        Code code = codeRepository.save(codeDto.toEntity());

        return CodeCreateResponse.builder()
                .id(code.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeFindAllResponse> findAllCode() {
        List<Code> codeList = codeRepository.findAll();

        return codeList.stream()
                .map(code -> CodeFindAllResponse.builder()
                        .id(code.getId())
                        .grpCd(code.getGrpCd())
                        .grpCdNm(code.getGrpCdNm())
                        .cd(code.getCd())
                        .cdNm(code.getCdNm())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CodeFindByIdResponse findCodeById(Long id) {
        try {
            Code code = codeRepository.findById(id).get();

            return CodeFindByIdResponse.builder()
                    .id(code.getId())
                    .grpCd(code.getGrpCd())
                    .grpCdNm(code.getGrpCdNm())
                    .cd(code.getCd())
                    .cdNm(code.getCdNm())
                    .build();
        } catch (NoSuchElementException e) {
            throw new BusinessExceptionHandler("존재하지 않는 code id: " + id, ErrorCode.NOT_FOUND_ERROR);
        }
    }
}
