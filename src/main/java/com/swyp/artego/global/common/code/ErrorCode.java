package com.swyp.artego.global.common.code;

import lombok.Getter;

/**
 * [공통 코드] API 통신에 대한 '에러 코드'를 Enum 형태로 관리한다.
 *
 */
@Getter
public enum ErrorCode {
    /**
     * ******************************* Global Error CodeList ***************************************
     * HTTP Status Code
     * 400 : Bad Request
     * 401 : Unauthorized
     * 403 : Forbidden
     * 404 : Not Found
     * 500 : Internal Server Error
     * *********************************************************************************************
     */

    /*
     400 Bad Request
     */
    BAD_REQUEST_ERROR(400, "G001", "잘못된 서버 요청"),

    INVALID_FILE(400, "B998", "올바르지 않은 파일"),

    INVALID_TYPE_VALUE(400, "G003", "유효하지 않은 타입"),

    JSON_PARSE_ERROR(400, "G006", "JsonParseException - com.google.gson JSON 파싱 실패"),

    JACKSON_PROCESS_ERROR(400, "G007", "com.fasterxml.jackson.core Processing Error"),

    MISSING_REQUEST_PARAMETER_ERROR(400, "G004", "Request Parameter 로 데이터가 전달되지 않을 경우"),

    MISSING_REQUEST_PART_ERROR(400, "G014", "요청에 필수 part가 누락되었습니다."),

    NOT_VALID_ERROR(400, "G011", "파라미터나 객체의 값이 유효하지 않은 경우"),

    NOT_VALID_HEADER_ERROR(400, "G012", "헤더에 데이터가 존재하지 않는 경우"), // 인증과 관련된 헤더는 401/403으로 따로 만들기

    REQUEST_BODY_MISSING_ERROR(400, "G002", "요청 본문이 없거나 읽을 수 없습니다."),

    /*
     401 Unauthorized
     */
    // 인증되지 않은 사용자 접근 (로그인 필요)
    UNAUTHORIZED_ERROR(401, "G013", "Unauthorized Access – 인증(로그인)이 필요합니다."),

    /*
     403 Forbidden
     */
    FORBIDDEN_ERROR(403, "G008", "권한이 없음"),

    /*
     404 Not Found
     */
    NOT_FOUND_ERROR(404, "G009", "서버로 요청한 리소스가 존재하지 않음"),

    /*
     409 Conflict (중복 자원 에러)
     */
    DUPLICATE_RESOURCE(409, "G015", "이미 존재하는 리소스입니다."),

    /*
     500 Internal Server Error
     */
    NULL_POINT_ERROR(500, "G010", "NULL Point Exception 발생"),

    IO_ERROR(500, "G005", "I/O Exception. 입력/출력 값이 유효하지 않음"),

    // Amazon S3 에러 -> 백엔드 로그 확인 필요
    AMAZON_S3_API_ERROR(500, "B997", "NCP Storage에서 사용하는 Amazon S3 API의 에러"), // 일단은 500 처리

    // 서버가 처리 할 방법을 모르는 경우 발생
    INTERNAL_SERVER_ERROR(500, "G999", "Internal Server Error Exception"),


    /**
     * ******************************* Custom Error CodeList ***************************************
     */
    // Transaction Insert Error
    INSERT_ERROR(200, "9999", "Insert Transaction Error Exception"),

    // Transaction Update Error
    UPDATE_ERROR(200, "9999", "Update Transaction Error Exception"),

    // Transaction Delete Error
    DELETE_ERROR(200, "9999", "Delete Transaction Error Exception"),

    ; // End

    private final int status;
    private final String divisionCode;
    private final String message;

     ErrorCode(int status, String divisionCode, String message) {
        this.status = status;
        this.divisionCode = divisionCode;
        this.message = message;
    }
}
