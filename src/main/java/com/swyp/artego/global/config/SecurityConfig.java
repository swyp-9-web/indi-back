package com.swyp.artego.global.config;

import com.swyp.artego.global.auth.oauth.handler.CustomAccessDeniedHandler;
import com.swyp.artego.global.auth.oauth.handler.CustomLogoutSuccessHandler;
import com.swyp.artego.global.auth.oauth.handler.LoginSuccessHandler;
import com.swyp.artego.global.auth.oauth.handler.UnauthorizedAccessHandler;
import com.swyp.artego.global.auth.oauth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthService authService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    private final UnauthorizedAccessHandler unauthorizedAccessHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(authService))
                        .successHandler(loginSuccessHandler)
                )

                .authorizeHttpRequests(auth -> auth
                        // 1. 파일 API - 모두 허용
                        .requestMatchers("/api/test/file/**").permitAll()

                        // 2. 작품 API
                        .requestMatchers("/api/v1/items/search", "/api/v1/items/{itemId}").permitAll() // 검색/세부조회
                        .requestMatchers("/api/v1/items").hasRole("ARTIST") // 등록
                        .requestMatchers("/api/v1/items/{itemId}").hasRole("ARTIST") // 삭제/수정

                        // 3. 댓글 API
                        .requestMatchers("/api/v1/comments/item/**").permitAll() // 작품별 댓글 전체 조회
                        .requestMatchers("/api/v1/comments/**").authenticated() // 나머지 전부 로그인 필요

                        // 4. 유저 API
                        .requestMatchers("/api/v1/users/me", "/api/v1/users/profile/**").authenticated() // 프로필 수정, 내 정보 조회
                        .requestMatchers("/api/v1/users/check-nickname", "/api/v1/users/artists/**").permitAll() // 닉네임 중복, 작가 정보 조회

                        // 5. 스크랩 API - 로그인 필요
                        .requestMatchers("/api/v1/scraps/**").authenticated()

                        // 6. 팔로우 API - 로그인 필요
                        .requestMatchers("/api/v1/follows/**").authenticated()

                        // 7. 이모지 API - 로그인 필요
                        .requestMatchers("/api/v1/item-emojis/**").authenticated()

                        // 8. 더미 API - 모두 허용
                        .requestMatchers("/api/dev/**").permitAll()

                        // 9. swagger - 모두 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()

                        // 기타 요청은 전부 거부
                        .anyRequest().denyAll()

                        // TODO: 그라파나,프로메테우스 모니터링 주소도 나중에 설정해줘야함!
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedAccessHandler)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                .sessionManagement(session -> session
                        .maximumSessions(3)
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

}
