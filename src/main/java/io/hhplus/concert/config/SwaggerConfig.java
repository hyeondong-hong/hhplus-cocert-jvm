package io.hhplus.concert.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Concert Reservation System API Document")
                .version("v1.0")
                .description("콘서트 예약 시스템 API 명세서");

        String uuidSchemeName = "Authorization";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(uuidSchemeName);
        Components components = new Components()
                .addSecuritySchemes(uuidSchemeName, new SecurityScheme()
                        .name(uuidSchemeName)
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER));

        return new OpenAPI()
                .components(new Components())
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
