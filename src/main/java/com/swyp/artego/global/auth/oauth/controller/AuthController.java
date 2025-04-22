package com.swyp.artego.global.auth.oauth.controller;


import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    // 이해 돕기 위한 테스트용! 테스트하고 지워도된다.

    private final UserRepository userRepository;


    @GetMapping("/login/naver")
    public String redirectToNaverOAuth() {
        return "redirect:/oauth2/authorization/naver";
    }



    @GetMapping("/")
    public String home() {
    return "홈입니다. 로그인 없이 접근 가능합니다.";
    }


    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal AuthUser user) {
        String oauthId = user.getOauthId(); // ex: "naver 123456"
        String name = user.getName();         // ex: 홍길동

        User longin_user = userRepository.findByOauthId(oauthId).get();


        System.out.println("로그인 제대로 됐다! " + "  "+ longin_user.getName());

        return ResponseEntity.ok(Map.of(
                "username", oauthId,
                "name", name
        ));
    }



}
