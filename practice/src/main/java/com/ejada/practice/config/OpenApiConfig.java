package com.ejada.practice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI / OpenAPI documentation configuration.
 * <p>Exposes the interactive API documentation at {@code /swagger-ui.html}
 * (renders {@code /swagger-ui/index.html}) and the raw OpenAPI JSON at
 * {@code /v3/api-docs}. Registers a reusable {@code bearerAuth} security
 * scheme so protected endpoints can be exercised directly from Swagger UI
 * by supplying a JWT obtained from {@code POST /api/auth/login}.</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    /**
     * Builds the {@link OpenAPI} bean describing this service.
     * @return the fully configured OpenAPI definition
     */
    @Bean
    public OpenAPI practiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Practice E-Commerce API")
                        .description("REST API for authentication, user management, "
                                + "product catalogue, categories, and order processing.")
                        .version("v1")
                        .contact(new Contact().name("EJADA Practice Project")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
