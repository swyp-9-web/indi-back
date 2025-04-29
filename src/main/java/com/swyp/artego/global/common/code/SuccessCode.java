package com.swyp.artego.global.common.code;

import lombok.Getter;

/**
 * [공통 코드] API 통신에 대한 '에러 코드' Enum 형태로 관리한다.
 */
@Getter
public enum SuccessCode {

    // DB
    SELECT_SUCCESS(200, "200", "SELECT_SUCCESS"),
    DELETE_SUCCESS(200, "200", "DELETE_SUCCESS"),
    INSERT_SUCCESS(201, "201", "INSERT_SUCCESS"),
    UPDATE_SUCCESS(204, "204", "UPDATE_SUCCESS"),

    // Login
    LOGIN_SUCCESS(200, "200", "로그인 성공"),
    LOGOUT_SUCCESS(200, "200", "로그아웃 성공"),

    FILE_UPLOAD_SUCCESS(200, "200", "파일 업로드 성공"),
    FILE_DELETE_SUCCESS(200, "200", "파일 삭제 성공")

    ;

    private final int status;
    private final String code;
    private final String message;

    SuccessCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
