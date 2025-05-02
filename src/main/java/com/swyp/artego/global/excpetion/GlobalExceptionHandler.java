package com.swyp.artego.global.excpetion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.common.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParseException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;

/**
 * Controller 내에서 발생하는 Exception을 Catch하여 Response를 보내주는 기능을 수행한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final HttpStatus HTTP_STATUS_OK = HttpStatus.OK;

    /**
     * ******************** business Layer에서 발생하는 오류에 대해서 공통 처리 ********************
     */

    /**
     * BusinessException에서 발생한 에러
     *
     * @param ex BusinessException
     * @return ResponseEntity
     */
    @ExceptionHandler(BusinessExceptionHandler.class)
    public ResponseEntity<ErrorResponse> handleCustomException(BusinessExceptionHandler ex){
        final ErrorResponse response = ErrorResponse.of(ex.getErrorCode(), ex.getMessage());
        HttpStatus status = HttpStatus.resolve(ex.getErrorCode().getStatus());
        return new ResponseEntity<>(response, status);
    }

    /**
     * ******************** Controller에서 발생하는 Exception을 Global Exception ********************
     */

    /**
     * [Exception] API 호출 시 Validator를 수동 호출하거나, @Validated가 붙은 메서드 파라미터(@RequestParam, @PathVariable 등)에서 유효성 검증 실패 시 발생
     * 발생 조건: 수동 Validator.validate() 사용, @Validated + @RequestParam, @PathVariable 등
     *
     * @param ex ConstraintViolationException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("handleConstraintViolationException 발생", ex);
        StringBuilder stringBuilder = new StringBuilder();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            stringBuilder.append(violation.getPropertyPath())
                    .append(": ")
                    .append(violation.getMessage())
                    .append(", ");
        }

        final ErrorResponse response = ErrorResponse.of(ErrorCode.NOT_VALID_ERROR, stringBuilder.toString());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * [Exception] API 호출 시 '객체' 혹은 '파라미터' 데이터 값이 유효하지 않은 경우
     * 발생 조건: @RequestParam, @RequestPart, @PathVariable 등 메서드 파라미터 직접 유효성 검증 시
     *
     * @param ex HandlerMethodValidationException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    protected ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        log.error("HandlerMethodValidationException 발생", ex);

        StringBuilder stringBuilder = new StringBuilder();

        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                stringBuilder.append(error.getDefaultMessage()).append(", ");
            }
        }

        // 교차 파라미터 검사 결과도 포함 (optional)
        for (MessageSourceResolvable error : ex.getCrossParameterValidationResults()) {
            stringBuilder.append(error.getDefaultMessage()).append(", ");
        }

        final ErrorResponse response = ErrorResponse.of(ErrorCode.BAD_REQUEST_ERROR, stringBuilder.toString());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * [Exception] API 호출 시 '객체' 혹은 '파라미터' 데이터 값이 유효하지 않은 경우
     * 발생 조건: @RequestBody, @ModelAttribute
     *
     * @param ex MethodArgumentNotValidException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        log.error("handleMethodArgumentNotValidException 발생", ex);
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder stringBuilder = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()){
            stringBuilder.append(fieldError.getField()).append(":");
            stringBuilder.append(fieldError.getDefaultMessage());
            stringBuilder.append(", ");
        }
        final ErrorResponse response = ErrorResponse.of(ErrorCode.NOT_VALID_ERROR, String.valueOf(stringBuilder));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * [Exception] multipart/form-data 요청에서 필수 RequestPart 누락 시 발생
     * TODO: multipart/form-data 요청 외에 MissingServletRequestPartException 발생 시, 주석 변경
     *
     * @param ex MissingServletRequestPartException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        log.error("MissingServletRequestPartException 발생 - 누락된 part: {}", ex.getRequestPartName(), ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.MISSING_REQUEST_PART_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * [Exception] API 호출 시 'Header' 내에 데이터 값이 유효하지 않은 경우
     *
     * @param ex MissingRequestHeaderException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    protected ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex){
        log.error("MissingRequestHeaderException 발생", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.REQUEST_BODY_MISSING_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * [Exception] 요청 본문(JSON)이 누락되었거나 파싱에 실패한 경우
     *
     * @param ex HttpMessageNotReadableException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException 발생", ex);
        Throwable cause = ex.getCause();
        ErrorResponse response;
        if (cause instanceof InvalidFormatException formatEx) {
            Class<?> targetType = formatEx.getTargetType();
            if (targetType.isEnum()) {
                response = ErrorResponse.of(ErrorCode.JSON_PARSE_ERROR, ex.getMessage());
            } else {
                response = ErrorResponse.of(ErrorCode.INVALID_TYPE_VALUE, ex.getMessage());
            }
        } else if (cause instanceof MismatchedInputException) {
            response = ErrorResponse.of(ErrorCode.INVALID_TYPE_VALUE, ex.getMessage());
        } else {
            response = ErrorResponse.of(ErrorCode.REQUEST_BODY_MISSING_ERROR, ex.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * [Exception] 클라이언트에서 request로 '파라미터로' 데이터가 넘어오지 않았을 경우
     *
     * @param ex MissingServletRequestParameterException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingRequestHeaderExceptionException(
            MissingServletRequestParameterException ex) {
        log.error("handleMissingServletRequestParameterException 발생", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.MISSING_REQUEST_PARAMETER_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    /**
     * [Exception] 잘못된 서버 요청일 경우 발생한 경우
     *
     * @param e HttpClientErrorException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(HttpClientErrorException.BadRequest.class)
    protected ResponseEntity<ErrorResponse> handleBadRequestException(HttpClientErrorException e) {
        log.error("HttpClientErrorException.BadRequest", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.BAD_REQUEST_ERROR, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    /**
     * [Exception] 잘못된 주소로 요청 한 경우
     *
     * @param e NoHandlerFoundException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNoHandlerFoundExceptionException(NoHandlerFoundException e) {
        log.error("handleNoHandlerFoundExceptionException 발생", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.NOT_FOUND_ERROR, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    /**
     * [Exception] NULL 값이 발생한 경우
     *
     * @param e NullPointerException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {
        log.error("handleNullPointerException 발생", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.NULL_POINT_ERROR, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Input / Output 내에서 발생한 경우
     *
     * @param ex IOException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(IOException.class)
    protected ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        log.error("handleIOException 발생", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.IO_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * com.google.gson 내에 Exception 발생하는 경우
     *
     * @param ex JsonParseException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(JsonParseException.class)
    protected ResponseEntity<ErrorResponse> handleJsonParseExceptionException(JsonParseException ex) {
        log.error("handleJsonParseExceptionException 발생", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.JSON_PARSE_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * com.fasterxml.jackson.core 내에 Exception 발생하는 경우
     *
     * @param ex JsonProcessingException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(JsonProcessingException.class)
    protected ResponseEntity<ErrorResponse> handleJsonProcessingException(JsonProcessingException ex) {
        log.error("handleJsonProcessingException 발생", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.REQUEST_BODY_MISSING_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    // ==================================================================================================================

    /**
     * [Exception] 모든 Exception 경우 발생
     *
     * @param ex Exception
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(Exception.class)
    protected final ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        log.error("Exception 발생", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
