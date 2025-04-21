package com.swyp.artego.global.common.response;

import lombok.Builder;
import lombok.Getter;

/**
 * [공통] API Response 결과의 반환 값을 관리한다.
 */
@Getter
public class ApiResponse<T> {

    private T result;
    private int resultCode;
    private String resultMessage;

    @Builder
    public ApiResponse(final T result, final int resultCode, final String resultMessage){
        this.result = result;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }
}
