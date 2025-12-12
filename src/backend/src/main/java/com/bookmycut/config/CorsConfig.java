package com.bookmycut.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing) para permitir
 * solicitudes desde el frontend Angular.
 * Similar a la configuración de dwese-ticket-logger-api.
 */
@Configuration
public class CorsConfig {

    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

    @Value("${cors.allowed-origins:*}")
    private String[] allowedOrigins;

    /**
     * Configura CORS para permitir solicitudes desde los orígenes especificados.
     * Usa WebMvcConfigurer para configurar CORS en Spring MVC.
     *
     * @return WebMvcConfigurer con la configuración de CORS.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(allowedOrigins) // Usa patterns para más flexibilidad
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
                        .exposedHeaders("Authorization", "Content-Type", "X-Total-Count")
                        .allowCredentials(true)
                        .maxAge(3600); // Cache preflight por 1 hora
            }
            
            @Override
            public void configureMessageConverters(java.util.List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
                // Asegurar que todas las respuestas de texto usen UTF-8
                org.springframework.http.converter.StringHttpMessageConverter stringConverter = 
                    new org.springframework.http.converter.StringHttpMessageConverter(java.nio.charset.StandardCharsets.UTF_8);
                stringConverter.setWriteAcceptCharset(false);
                converters.add(0, stringConverter);
            }
        };
    }

    /**
     * Configuración de CORS para Spring Security.
     * Proporciona un CorsConfigurationSource que puede ser usado por SecurityConfig.
     *
     * @return CorsConfigurationSource con la configuración de CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(Arrays.asList(allowedOrigins)); // Usa patterns para más flexibilidad
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Especificar headers permitidos explícitamente en lugar de "*" para mayor seguridad
        corsConfig.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        
        // Headers expuestos al frontend
        corsConfig.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count"
        ));
        
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L); // Cache preflight requests por 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return source;
    }

    /**
     * Valida la configuración de CORS al iniciar la aplicación.
     */
    @PostConstruct
    public void validateCorsConfiguration() {
        if (allowedOrigins == null || allowedOrigins.length == 0) {
            logger.warn("CORS: No se han configurado orígenes permitidos. Usando '*' por defecto.");
        } else {
            // Validar formatos de URL
            for (String origin : allowedOrigins) {
                if (!origin.equals("*") && !origin.matches("^https?://.*")) {
                    logger.error("CORS origin inválido: {}. Debe ser '*' o una URL válida (http://... o https://...)", origin);
                    throw new IllegalStateException("CORS origin inválido: " + origin);
                }
            }
            logger.info("CORS configurado correctamente con {} orígenes permitidos", allowedOrigins.length);
        }
    }
}

