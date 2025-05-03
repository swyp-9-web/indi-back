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



        // 2. 도메인 및 환경 판단
        URI uri = URI.create(redirectUri);
        String domain = uri.getHost(); // ex: localhost, fe.site.com
        boolean isLocalhost = domain.contains("localhost");
        boolean isHttps = redirectUri.startsWith("https");

        // 3. 세션ID 발급 → 쿠키 구성
        String sessionId = session.getId();

        StringBuilder cookieBuilder = new StringBuilder();
        cookieBuilder.append("JSESSIONID=").append(sessionId)
                .append("; Path=/")
                .append("; HttpOnly");

        //  로컬이면 SameSite=Lax
        if (isLocalhost) {
            cookieBuilder.append("; SameSite=Lax");
        } else {
            //  운영 환경: SameSite=None + Secure + Domain 설정
            cookieBuilder.append("; SameSite=None");
            cookieBuilder.append("; Domain=").append(domain);

            if (isHttps) {
                cookieBuilder.append("; Secure");
            }
        }

        // 4. 쿠키 설정
        response.setHeader("Set-Cookie", cookieBuilder.toString());

        // 5. 리다이렉트
        response.sendRedirect(redirectUri);
    }


}
