package com.bookmycut.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para cargar variables de entorno desde un archivo .env.
 * Esto permite mantener las credenciales y configuraciones sensibles
 * fuera del código fuente.
 */
@Configuration
public class DotenvConfig {

    /**
     * Crea un bean de Dotenv que carga las variables de entorno desde el archivo .env.
     * Si el archivo no existe, se cargarán las variables del sistema.
     *
     * @return Instancia de Dotenv configurada.
     */
    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure()
                .ignoreIfMissing()
                .load();
    }
}

