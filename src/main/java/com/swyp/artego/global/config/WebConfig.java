package com.swyp.artego.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // TODO: 설정 추후 변경
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    "https://indi-front.vercel.app",
                    "https://indi-front-*-leehj322s-projects.vercel.app",
                    "http://localhost:3000"
                )
                .allowedMethods("*")        // 모든 HTTP 메서드 허용
                .allowedHeaders("*")        // 모든 헤더 허용
                .allowCredentials(true)
                .maxAge(3600);
    }
}
