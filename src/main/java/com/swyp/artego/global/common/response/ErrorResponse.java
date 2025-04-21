package com.swyp.artego.global.common.response;

import com.swyp.artego.global.common.code.ErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {

    private int status;
    private String divisionCode;
    private String resultMessage;
    private List<FieldError> errors;
    private String reason;

    /**
     * ******************************* ErrorResponse 생성자 ***************************************
     */

    @Builder
    protected ErrorResponse(final ErrorCode code){
        this.status = code.getStatus();
        this.divisionCode = code.getDivisionCode();
        this.resultMessage = code.getMessage();
        this.errors = new ArrayList<>();
    }

    @Builder
    protected ErrorResponse(final ErrorCode code, final String reason){
        this.status = code.getStatus();
        this.divisionCode = code. getDivisionCode();
        this.resultMessage = code.getMessage();
        this.reason = reason;
    }

    @Builder
    protected ErrorResponse(final ErrorCode code, final List<FieldError> errors){
        this.status = code.getStatus();
        this.divisionCode = code.getDivisionCode();
        this.resultMessage = code.getMessage();
        this.errors = errors;
    }

    /**
     * ******************************* Global Exception 전송 타입 ***************************************
     */

    public static ErrorResponse of(final ErrorCode code, final BindingResult bindingResult){
        return new ErrorResponse(code, FieldError.of(bindingResult));
    }

    public static ErrorResponse of(final ErrorCode code) {
        return new ErrorResponse(code);
    }

    public static ErrorResponse of(final ErrorCode code, final String reason){
        return new ErrorResponse(code, reason);
    }

    /**
     * 에러를 e.getBindingResult() 형태로 전달 받는 경우 해당 내용을 상세 내용으로 변경하는 기능을 수행한다.
     */
    public static class FieldError {
        private final String field;
        private final String value;
        private final String reason;

        public static List<FieldError> of(final String field, final String value, final String reason){
            List<FieldError> fieldErrors = new ArrayList<>();
            fieldErrors.add(new FieldError(field, value, reason));
            return fieldErrors;
        }

        private static List<FieldError> of(final BindingResult bindingResult){
            final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> {
                        error.getRejectedValue();
                        return new FieldError(
                                error.getField(),
                                error.getRejectedValue().toString(),
                                error.getDefaultMessage());
                    })
                    .collect(Collectors.toList());
        }

        @Builder
        public FieldError(String field, String value, String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }
    }
}
