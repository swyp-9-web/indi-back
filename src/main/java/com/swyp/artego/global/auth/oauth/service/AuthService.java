package com.swyp.artego.global.auth.oauth.service;

import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.enums.Role;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.dto.response.NaverOAuth2Response;
import com.swyp.artego.global.auth.oauth.dto.response.OAuth2Response;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    private static final int MAX_NICKNAME_RETRY = 2;

    //  loadUser() 메서드는 Spring Security 내부에서 자동으로 호출되는 메서드입니다.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = null;

        if (provider.equals("naver")) {
            oAuth2Response = new NaverOAuth2Response(oAuth2User.getAttributes());
        } else if (provider.equals("kakao")) {
            // oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        String oauthId = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        Optional<User> userOptional = userRepository.findByOauthId(oauthId);

        String role;

        if (userOptional.isPresent()) {
            // 유저가 존재하면 DB에서 Role을 가져와서 Prefix 추가
            Role userRole = userOptional.get().getRole();  // <- User 객체에 getRole()이 있어야 함
            role = "ROLE_" + userRole.name();
        } else {
            // 새 유저는 기본적으로 USER
            createUser(oAuth2Response, oauthId);
            role = "ROLE_USER";
        }

        return new AuthUser(oAuth2Response, role);
    }




    @Transactional
    protected void createUser(OAuth2Response oAuth2Response, String oauthId) {
        int retry = 0;

        while (retry++ < MAX_NICKNAME_RETRY) {
            String nickname;

            //  중복 아닐 때까지 무한 생성
            do {
                nickname = generateRandomNickname();
            } while (userRepository.existsByNickname(nickname));

            try {
                User newUser = User.builder()
                        .oauthId(oauthId)
                        .name(oAuth2Response.getName())
                        .email(oAuth2Response.getEmail())
                        .telNumber(oAuth2Response.getPhoneNumber())
                        .nickname(nickname)
                        .build();

                userRepository.save(newUser);
                return;
            } catch (DataIntegrityViolationException e) {
                // 저장 실패 (Race Condition) → 재시도
            }
        }

        throw new ServiceException("닉네임 생성 실패: 중복 또는 저장 실패로 인해 생성 불가");
    }


    private String generateRandomNickname() {
        return "user#" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

}