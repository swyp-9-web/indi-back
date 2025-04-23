package com.swyp.artego.global.config;

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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // OAuth2 설정 (소셜로그인 통신 후 받은 유저 데이터를 authService 여기서 처리)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(authService))
                        .successHandler(loginSuccessHandler)
                )

                // 권한 설정 (경로 허용 유무)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/oauth2/**", "/login/**").permitAll()
                        .anyRequest().authenticated()
                )

                // (로그인 x)인증 실패 시 JSON 응답
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedAccessHandler)
                )

                .sessionManagement(session -> session
                        .maximumSessions(3)
                )

                //  로그아웃 처리
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );


        return http.build();
    }
}
