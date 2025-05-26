package com.swyp.artego.global.auth.oauth.controller;


import com.swyp.artego.global.auth.oauth.service.AuthService;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller // 이 부분은 리다이렉트용으로 유지
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "OAuth 인증 및 권한 관련 API")
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login/naver")
    @Operation(summary = "네이버 OAuth2 로그인 리다이렉트")
    public String redirectToNaverOAuth(HttpServletRequest request,
                                       @RequestParam("redirect_uri") String redirectUri) {
        request.getSession().setAttribute("redirect_uri", redirectUri);
        return "redirect:/oauth2/authorization/naver";
    }
    @PatchMapping("/refresh-role")
    @ResponseBody
    @Operation(summary = "세션 내 권한 정보 갱신")
    public ResponseEntity<ApiResponse<String>> refreshRole(Authentication currentAuth) {
        authService.refreshAuthentication(currentAuth);

        return ResponseEntity.status(SuccessCode.UPDATE_SUCCESS.getStatus())
                .body(ApiResponse.<String>builder()
                        .result("권한이 성공적으로 갱신되었습니다.")
                        .resultCode(Integer.parseInt(SuccessCode.UPDATE_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.UPDATE_SUCCESS.getMessage())
                        .build());
    }

    @GetMapping("/")
    @Operation(summary = "홈 진입 테스트 API")
    public String home() {
        return "home";
    }

}
