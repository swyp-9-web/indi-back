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

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 1. 프론트에서 받은 redirect_uri
        HttpSession session = request.getSession();
        String redirectUri = (String) session.getAttribute("redirect_uri");
        session.removeAttribute("redirect_uri");

        // 2. 도메인 추출
        URI uri = URI.create(redirectUri);
        String domain = uri.getHost(); // ex: fe.site.com

        // 3. 환경 판단: HTTP인지 HTTPS인지 (localhost면 HTTP로 간주)
        boolean isHttps = redirectUri.startsWith("https");

        // 4. 세션ID 발급 → 쿠키 구성
        String sessionId = session.getId();

        StringBuilder cookieBuilder = new StringBuilder();
        cookieBuilder.append("JSESSIONID=").append(sessionId)
                .append("; Path=/")
                .append("; Domain=").append(domain)
                .append("; HttpOnly")
                .append("; SameSite=None");

        //  HTTPS 환경에서만 Secure 설정 (지금은 HTTP니까 생략됨)
        if (isHttps) {
            cookieBuilder.append("; Secure");
        }

        // 5. 쿠키 설정
        response.setHeader("Set-Cookie", cookieBuilder.toString());

        // 6. 프론트로 리다이렉트
        response.sendRedirect(redirectUri);
    }

}
