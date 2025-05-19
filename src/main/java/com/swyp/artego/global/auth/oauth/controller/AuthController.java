package com.swyp.artego.global.auth.oauth.controller;


import com.swyp.artego.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {



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
