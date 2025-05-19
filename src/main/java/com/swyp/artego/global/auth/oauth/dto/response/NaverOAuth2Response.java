package com.swyp.artego.global.auth.oauth.dto.response;

import lombok.Getter;

import java.util.Map;

/**
 * 네이버 사용자 정보 응답 파싱 클래스
 */

public class NaverOAuth2Response implements OAuth2Response {

    private final Map<String, Object> attributes;

    public NaverOAuth2Response(Map<String, Object> attributes) {
        // "response" 키 안에 실제 사용자 정보가 들어 있음
        this.attributes = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getPhoneNumber() {
        return (String) attributes.get("mobile");
    }
}