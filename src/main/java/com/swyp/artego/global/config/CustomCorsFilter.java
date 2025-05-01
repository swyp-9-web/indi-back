package com.swyp.artego.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * WebMvcConfigurer 구현 대신 CORS Filter 를 커스텀.
 *
 * 기존 CorsRegistry 는 점(.) 단위의 세그먼트만 와일드카드로 처리할 수 있다.
 * 복잡한 정규식을 사용하기 위해서는 OncePerRequestFilter 를 구현해야 한다.
 */
@Component
public class CustomCorsFilter extends OncePerRequestFilter {

    private static final Set<String> STATIC_ALLOWED_ORIGINS = Set.of(
            "https://indi-front.vercel.app",
            "http://localhost:3000"
    );

    private static final Pattern DYNAMIC_ALLOWED_ORIGIN_PATTERN = Pattern.compile(
            "^https://indi-front-[a-z0-9]{9}-leehj322s-projects\\.vercel\\.app$"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        String origin = request.getHeader("Origin");

        boolean isAllowed = origin != null &&
                (STATIC_ALLOWED_ORIGINS.contains(origin) ||
                        DYNAMIC_ALLOWED_ORIGIN_PATTERN.matcher(origin).matches());

        if (isAllowed) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", "*");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
