package com.swyp.artego.global.common.annotation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class NullableNotBlankValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("null값은 검증을 통과한다")
    void nullValue() {
        TestDto dto = new TestDto(null);
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("빈 문자열은 검증에서 실패한다")
    void emptyString() {
        TestDto dto = new TestDto("");
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("null은 허용되지만, 공백만 있는 문자열은 허용되지 않습니다.");
    }

    @Test
    @DisplayName("공백 문자열은 검증에서 실패한다")
    void blankString() {
        TestDto dto = new TestDto("");
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("null은 허용되지만, 공백만 있는 문자열은 허용되지 않습니다.");
    }

    @Test
    @DisplayName("정상적인 문자열은 검증을 통과한다")
    void validString() {
        TestDto dto = new TestDto("유효한 댓글");
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }
}