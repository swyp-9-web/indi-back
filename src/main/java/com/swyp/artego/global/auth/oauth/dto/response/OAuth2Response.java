package com.swyp.artego.global.auth.oauth.dto.response;

/**
 * 모든 OAuth2 제공자에 대해 통일된 사용자 정보 인터페이스
 */
public interface OAuth2Response {

    String getProvider();      // 제공자: naver, kakao 등
    String getProviderId();   // 제공자별 고유 사용자 ID
    String getEmail();        // 이메일
    String getName();         // 이름
    String getPhoneNumber(); // 핸드폰 번호
}