package com.swyp.artego.global.auth.oauth.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        // 인증은 됐지만 권한이 부족할 때 → 403 + FORBIDDEN_ERROR
        ErrorResponse forbiddenError = ErrorResponse.of(ErrorCode.FORBIDDEN_ERROR);

        response.setStatus(ErrorCode.FORBIDDEN_ERROR.getStatus());
        response.setContentType("application/json; charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), forbiddenError);
    }
}