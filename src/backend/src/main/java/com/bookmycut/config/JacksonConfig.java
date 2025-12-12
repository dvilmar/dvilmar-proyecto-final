package com.bookmycut.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.nio.charset.StandardCharsets;

/**
 * Configuración de Jackson para asegurar UTF-8 y manejo correcto de enums
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder
                .modules(new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        
        // Asegurar que los enums se serialicen como strings
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        
        return objectMapper;
    }
    
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        return converter;
    }
}




