package com.swyp.artego.global.common.annotation;

public class TestDto {

    @NullableNotBlank
    private String comment;

    public TestDto(String comment) {
        this.comment = comment;
    }
}
