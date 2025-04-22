package com.swyp.artego.global.auth.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .result(null)
                .resultCode(SuccessCode.LOGOUT_SUCCESS.getStatus())
                .resultMessage(SuccessCode.LOGOUT_SUCCESS.getMessage())
                .build();

        response.setStatus(SuccessCode.LOGOUT_SUCCESS.getStatus());
        response.setContentType("application/json; charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
