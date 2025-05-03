package com.swyp.artego.global.auth.oauth.controller;


import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    // 이해 돕기 위한 테스트용! 테스트하고 지워도된다.

    private final UserRepository userRepository;


    @GetMapping("/login/naver")
    public String redirectToNaverOAuth(HttpServletRequest request,
                                       @RequestParam("redirect_uri") String redirectUri) {
        request.getSession().setAttribute("redirect_uri", redirectUri);
        return "redirect:/oauth2/authorization/naver";
    }



    @GetMapping("/")
    public String home() {
    return "home";
    }



}
