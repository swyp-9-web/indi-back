package com.swyp.artego.global.auth.oauth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 1. 세션에서 redirect URI 꺼냄
        HttpSession session = request.getSession(true);
        String redirectUri = (String) session.getAttribute("redirect_uri");
        session.removeAttribute("redirect_uri");

        // 1-1. redirectUri 값이 없거나 잘못된 경우 에러 처리
        if (redirectUri == null || redirectUri.isBlank()) {
            String errorMessage = " Missing redirect_uri in session. Received: " + redirectUri;
            log.error(errorMessage);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
            return;
        }

        URI uri;
        try {
            uri = URI.create(redirectUri);
            if (uri.getHost() == null) {
                String errorMessage = " Invalid redirectUri (no host): " + redirectUri;
                log.error(errorMessage);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
                return;
            }
        } catch (IllegalArgumentException e) {
            String errorMessage = " Malformed redirectUri: " + redirectUri;
            log.error(errorMessage, e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
            return;
        }

        boolean isLocalhost = uri.getHost().contains("localhost");

        // 2. Set-Cookie 헤더 보정
        Collection<String> setCookieHeaders = response.getHeaders("Set-Cookie");
        for (String header : setCookieHeaders) {
            if (header.startsWith("JSESSIONID")) {
                String updatedHeader = header;
                if (isLocalhost) {
                    updatedHeader += "; SameSite=Lax";
                } else {
                    updatedHeader += "; SameSite=None; Secure";
                }
                response.setHeader("Set-Cookie", updatedHeader);
                break;
            }
        }

        // 3. 세션 ID 전달을 위한 리다이렉션 URI 생성
        String sessionId = session.getId();
        String updatedRedirectUri = UriComponentsBuilder
                .fromUriString(redirectUri)
                .queryParam("sessionId", sessionId)
                .build()
                .toUriString();

        log.info(" Redirecting to URI: {}", updatedRedirectUri);

        // 4. 리다이렉션 수행
        response.sendRedirect(updatedRedirectUri);
    }
}
