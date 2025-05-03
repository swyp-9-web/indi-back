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
                //.allowedOriginPatterns("*")
                .allowedOrigins("http://localhost:3000") //  정확한 Origin만 허용!
                .allowedMethods("*")        // 모든 HTTP 메서드 허용
                .allowedHeaders("*")        // 모든 헤더 허용
                .allowCredentials(true)
                .maxAge(3600);
    }


}
