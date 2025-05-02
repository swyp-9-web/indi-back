package com.swyp.artego.global.dummy.controller;

import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import com.swyp.artego.global.dummy.dto.DummyOAuth2Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev")
public class DummyAuthController {

    private final UserRepository userRepository;

    @Operation(
            summary = "더미 유저 로그인",
            description = "테스트용 더미 유저를 생성하고 로그인합니다. 매번 새로운 더미 유저가 생성되며 Spring Security 세션에 등록됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "더미 유저 로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/auth/dummy-login")
    public ResponseEntity<ApiResponse<String>> loginWithRandomDummy(HttpSession session) {
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        String oauthId = "dummy " + randomId;
        String nickname = generateRandomNickname(); // 닉네임 생성기 사용

        // 1. 유저 생성 및 저장
        User user = User.builder()
                .oauthId(oauthId)
                .name(nickname)
                .email("dummy_" + randomId + "@test.com")
                .telNumber("010-0000-0000")
                .build();

        userRepository.save(user);

        // 2. AuthUser 생성
        DummyOAuth2Response dummyResponse = new DummyOAuth2Response(
                randomId,
                user.getName(),
                user.getEmail(),
                user.getTelNumber()
        );
        AuthUser authUser = new AuthUser(dummyResponse, "ROLE_USER");

        // 3. Spring Security Context에 인증 정보 등록
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. 세션에 Security Context 저장
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // 5. 응답
        return ResponseEntity.status(SuccessCode.INSERT_SUCCESS.getStatus())
                .body(ApiResponse.<String>builder()
                        .result(oauthId)
                        .resultCode(Integer.parseInt(SuccessCode.INSERT_SUCCESS.getCode()))
                        .resultMessage("더미 유저 로그인 성공")
                        .build());
    }

    //  닉네임 생성기
    private String generateRandomNickname() {
        String[] first = {"밤하늘", "햇살", "잉크", "달빛", "감성", "수채화", "몽환", "종이", "무지개", "꽃잎"};
        String[] second = {"마녀", "소년", "고양이", "마법사", "정원사", "작가", "화가", "공방", "나무", "펜"};
        String word = first[(int) (Math.random() * first.length)] + second[(int) (Math.random() * second.length)];
        int number = (int) (Math.random() * 9000) + 1000; // 1000~9999
        return word + "_" + number;
    }
}
