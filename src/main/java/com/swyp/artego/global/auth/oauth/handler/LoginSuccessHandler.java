package com.swyp.artego.global.auth.oauth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 1. 세션 생성 및 redirect URI 꺼냄
        HttpSession session = request.getSession(true);
        String redirectUri = (String) session.getAttribute("redirect_uri");
        session.removeAttribute("redirect_uri");

        URI uri = URI.create(redirectUri);
        boolean isLocalhost = uri.getHost().contains("localhost");

        // 2. Spring이 자동 생성한 Set-Cookie 헤더 보정
        Collection<String> setCookieHeaders = response.getHeaders("Set-Cookie");

        for (String header : setCookieHeaders) {
            if (header.startsWith("JSESSIONID")) {
                String updatedHeader;

                if (isLocalhost) {
                    updatedHeader = header + "; SameSite=Lax";
                } else {
                    updatedHeader = header + "; SameSite=None; Secure";
                }

                response.setHeader("Set-Cookie", updatedHeader);
                break;
            }
        }

        // 3. 최종 리디렉션
        response.sendRedirect(redirectUri);
    }
}
