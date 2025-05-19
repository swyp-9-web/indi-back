package com.swyp.artego.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .version("v1.0")
                .title("Artego API")
                .description("Artego API");
        return new OpenAPI()
                .info(info)
                .components(
                        new Components()
                                .addSecuritySchemes("JSESSIONID", new SecurityScheme()
                                        .name("JSESSIONID")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                )
                );
    }

}
