package com.bookmycut.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI (Swagger) documentation.
 * Provides comprehensive API documentation with authentication support.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("BookMyCut API")
                        .version("1.0.0")
                        .description("""
                                API REST para BookMyCut - Sistema de gestión de reservas para peluquerías y centros de belleza.
                                
                                ## Características
                                - Autenticación JWT
                                - Gestión de usuarios (Clientes, Estilistas, Administradores)
                                - Gestión de citas
                                - Gestión de servicios
                                - Control de disponibilidad
                                
                                ## Autenticación
                                Para usar los endpoints protegidos, incluye el token JWT en el header:
                                ```
                                Authorization: Bearer <token>
                                ```
                                
                                Obtén el token mediante `/api/auth/login` o `/api/auth/register`.
                                """)
                        .contact(new Contact()
                                .name("BookMyCut Team")
                                .email("support@bookmycut.com")
                                .url("https://bookmycut.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + "/api")
                                .description("Servidor de desarrollo"),
                        new Server()
                                .url("https://api.bookmycut.com")
                                .description("Servidor de producción")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtenido mediante /api/auth/login o /api/auth/register")));
    }
}







