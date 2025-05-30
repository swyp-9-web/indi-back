package com.swyp.artego.global.auth.oauth.model;

import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.global.auth.oauth.dto.response.OAuth2Response;
import com.swyp.artego.global.auth.oauth.dto.response.SimpleOAuth2Response;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class AuthUser implements OAuth2User, UserDetails {

    private final OAuth2Response oAuth2Response;
    private final String role;


    /**
     * 이 생성자는 OAuth 로그인용
     */
    public AuthUser(OAuth2Response oAuth2Response, String role) {

        this.oAuth2Response = oAuth2Response;
        this.role = role;
    }

    /**
     * 이 생성자는 권한 갱신 API용
     */
    public AuthUser(User user) {
        this.oAuth2Response = new SimpleOAuth2Response(user); // User → OAuth2Response로 감싸줌
        this.role = "ROLE_" + user.getRole().name(); // DB에서 최신 권한 사용
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return role;
            }
        });

        return collection;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return oAuth2Response.getName();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    @Override
    public String getName() {
        return oAuth2Response.getName();
    }

    public String getOauthId() {
        return oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
    }


}

