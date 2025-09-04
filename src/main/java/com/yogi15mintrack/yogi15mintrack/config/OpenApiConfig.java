package com.yogi15mintrack.yogi15mintrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;



@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI api() {
        final String scheme = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Yogi15MinTrack API")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement()
                        .addList(scheme))
                .components(new Components()
                        .addSecuritySchemes(scheme, new SecurityScheme()
                                .name(scheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
