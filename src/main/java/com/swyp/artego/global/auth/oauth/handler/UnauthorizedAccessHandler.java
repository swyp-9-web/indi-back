package com.swyp.artego.global.auth.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UnauthorizedAccessHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 🔄 인증되지 않은 사용자 접근 시 → 401 + UNAUTHORIZED_ERROR
        ErrorResponse unauthorizedError = ErrorResponse.of(ErrorCode.UNAUTHORIZED_ERROR);

        response.setStatus(ErrorCode.UNAUTHORIZED_ERROR.getStatus());
        response.setContentType("application/json; charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), unauthorizedError);
    }
}
