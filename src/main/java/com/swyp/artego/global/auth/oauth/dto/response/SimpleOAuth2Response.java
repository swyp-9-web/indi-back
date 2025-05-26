package com.swyp.artego.global.auth.oauth.dto.response;

import com.swyp.artego.domain.user.entity.User;

public class SimpleOAuth2Response implements OAuth2Response {

    private final User user;

    public SimpleOAuth2Response(User user) {
        this.user = user;
    }

    @Override
    public String getProvider() {
        return user.getOauthId().split(" ")[0];
    }

    @Override
    public String getProviderId() {
        return user.getOauthId().split(" ")[1];
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public String getPhoneNumber() {
        return user.getTelNumber();
    }
}
