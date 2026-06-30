package com.school.platform.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
@Profile("!prod")
public class SwaggerConfig {
    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI schoolManagementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("School Management API")
                        .version("1.0")
                        .description("REST API for the primary school management system")
                        .contact(new Contact().name("Architecture Team")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
