package com.swyp.artego.global.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * null 과 공백이 아닌 문자열을 허용한다.
 * 수정 API의 Request DTO에서 사용한다.
 *
 * null, "example" 허용
 * "", " " 허용 X
 */
@Documented
@Constraint(validatedBy = NullableNotBlankValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface NullableNotBlank {

    String message() default "null은 허용되지만, 공백만 있는 문자열은 허용되지 않습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
