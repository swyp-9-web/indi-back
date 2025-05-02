package com.swyp.artego.global.dummy.dto;

import com.swyp.artego.global.auth.oauth.dto.response.OAuth2Response;

public class DummyOAuth2Response implements OAuth2Response {

    private final String providerId;
    private final String name;
    private final String email;
    private final String phoneNumber;

    public DummyOAuth2Response(String providerId, String name, String email, String phoneNumber) {
        this.providerId = providerId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getProvider() {
        return "dummy";
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }
}
