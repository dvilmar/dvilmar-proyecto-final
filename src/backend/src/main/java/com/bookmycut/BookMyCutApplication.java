package com.bookmycut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de la aplicación BookMyCut.
 * Habilita la auditoría JPA para rastrear automáticamente fechas de creación y modificación.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class BookMyCutApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookMyCutApplication.class, args);
    }
}

