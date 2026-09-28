package com.snaplink.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI snapLinkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SnapLink API — High-Performance URL Shortener & Telemetry Platform")
                        .description("Tài liệu đặc tả toàn bộ RESTful APIs của nền tảng SnapLink bao gồm Authentication, URL Shortening, Cache-Aside Redirect, Distributed Rate Limiting và Click Telemetry Analytics.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SnapLink Engineering Team")
                                .email("dev@snaplink.io")
                                .url("https://snaplink.io"))
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Nhập Bearer JWT access token để gọi các endpoint yêu cầu xác thực.")));
    }
}
