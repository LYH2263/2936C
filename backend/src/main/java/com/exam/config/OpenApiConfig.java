package com.exam.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        Schema<Instant> instantSchema = new Schema<>();
        instantSchema.setType("string");
        instantSchema.setFormat("date-time");
        instantSchema.setExample("2024-01-01T00:00:00Z");

        return new OpenAPI()
                .info(new Info()
                        .title("在线考试系统 API")
                        .version("1.0.0")
                        .description("在线考试系统后端 REST API 文档，包含考试管理、提交记录、用户管理、通知等模块。")
                        .contact(new Contact()
                                .name("Exam System Team")
                                .email("support@exam.local"))
                        .license(new License()
                                .name("Internal Use Only")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("开发环境")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authorization header using the Bearer scheme. 格式: 'Bearer {token}'"))
                        .addSchemas("Instant", instantSchema));
    }
}
