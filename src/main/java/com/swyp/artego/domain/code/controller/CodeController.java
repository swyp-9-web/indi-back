package com.swyp.artego.domain.code.controller;

import com.swyp.artego.domain.code.dto.request.CodeCreateRequest;
import com.swyp.artego.domain.code.dto.response.CodeCreateResponse;
import com.swyp.artego.domain.code.dto.response.CodeFindAllResponse;
import com.swyp.artego.domain.code.dto.response.CodeFindByIdResponse;
import com.swyp.artego.domain.code.service.CodeService;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    /**
     * [API] 코드 등록
     *
     * @return ResponseEntity<ApiResponse> : 응답 결과 및 응답 코드 반환
     */
    @PostMapping("/")
    public ResponseEntity<ApiResponse> createCode(
            @RequestBody @Valid CodeCreateRequest codeDto
    ) {
        log.debug("[CodeController] 코드를 등록합니다.");
        CodeCreateResponse res = codeService.createCode(codeDto);

        ApiResponse ar = ApiResponse.builder()
                .result(res)
                .resultCode(SuccessCode.INSERT_SUCCESS.getStatus())
                .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                .build();

        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

    /**
     * [API] 코드 전체 조회
     *
     * @return ResponseEntity<ApiResponse> : 응답 결과 및 응답 코드 반환
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse> findAllCode() {
        log.debug("[CodeController] 코드를 전체 조회합니다.");
        List<CodeFindAllResponse> res = codeService.findAllCode();

        ApiResponse ar = ApiResponse.builder()
                .result(res)
                .resultCode(SuccessCode.SELECT_SUCCESS.getStatus())
                .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                .build();

        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

    /**
     * [API] 코드 단건 조회, 조회 조건: id
     *
     * @param id 조회 조건
     * @return ResponseEntity<ApiResponse> : 응답 결과 및 응답 코드 반환
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findCodeById(@PathVariable Long id) {
        log.debug("[CodeController] 코드를 id를 조건으로 단건 조회합니다. id: {}", id);
        CodeFindByIdResponse res = codeService.findCodeById(id);

        ApiResponse ar = ApiResponse.builder()
                .result(res)
                .resultCode(SuccessCode.SELECT_SUCCESS.getStatus())
                .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                .build();

        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

}
