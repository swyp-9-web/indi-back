package com.swyp.artego.global.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullableNotBlankValidator implements ConstraintValidator<NullableNotBlank, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null은 허용
        if (value == null) {
            return true;
        }

        // 빈 문자열이거나 공백만 있는 경우는 허용하지 않음
        boolean res =  !value.trim().isEmpty();
        return res;
    }
}
