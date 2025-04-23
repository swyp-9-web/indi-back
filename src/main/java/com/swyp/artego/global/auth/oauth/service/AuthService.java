package com.swyp.artego.global.auth.oauth.service;

import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.dto.response.NaverOAuth2Response;
import com.swyp.artego.global.auth.oauth.dto.response.OAuth2Response;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;


    //  loadUser() 메서드는 Spring Security 내부에서 자동으로 호출되는 메서드입니다.
    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User.getAttributes());

        //플랫폼이 어디인지 구분해주는 값 ex naver, kakao
        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = null;

        if (provider.equals("naver")) {
            oAuth2Response = new NaverOAuth2Response(oAuth2User.getAttributes());
        }

        // 추후 확장성 고려
        else if (provider.equals("kakao")) {
            //oAuth2Response = new KakaoReponse(oAuth2User.getAttributes());
        }
        else {
            return null;
        }



        //추후 작성

        String role = "ROLE_USER"; // <- 추후 변경해야하는 코드 (까먹지말라고 그냥 써놓음) 이거 불변으로 하면안된다. 나중에 바꿔야함

        String oauthId = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        Optional<User> userOptional = userRepository.findByOauthId(oauthId);

        if (userOptional.isPresent()) {
            // 로그인 처리 (기존 유저 사용)
            User loginUser = userOptional.get();
            // SecurityContext 설정 등 로그인 로직 넣으면 돼요

            System.out.println("이미 존재하는 회원입니다.");

        }


        else {
            // 회원가입 처리

            User newUser = User.builder()
                    .oauthId(oauthId)
                    .name(oAuth2Response.getName())
                    .email(oAuth2Response.getEmail())
                    .telNumber(oAuth2Response.getPhoneNumber()) // 소셜 로그인 시 기본값 또는 별도 입력받기
                    .build();

            userRepository.save(newUser);

        }



        return new AuthUser(oAuth2Response, role);

    }
}