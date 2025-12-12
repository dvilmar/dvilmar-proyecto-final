package com.bookmycut.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PlainTextPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * Configura el filtro de seguridad para las solicitudes HTTP, especificando las
     * rutas permitidas y los roles necesarios para acceder a diferentes endpoints.
     *
     * @param http instancia de {@link HttpSecurity} para configurar la seguridad.
     * @return una instancia de {@link SecurityFilterChain} que contiene la configuración de seguridad.
     * @throws Exception si ocurre un error en la configuración de seguridad.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos - WebSocket (SockJS necesita /ws/info antes de la conexión)
                .requestMatchers("/ws/**").permitAll()
                // Endpoints públicos - Swagger/OpenAPI (sin /api porque context-path ya lo añade)
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                // Endpoints públicos - Autenticación (login, register y logout son públicos)
                .requestMatchers("/auth/login", "/auth/register", "/auth/logout").permitAll()
                
                // Endpoints públicos - Creación de citas sin autenticación
                .requestMatchers(HttpMethod.POST, "/citas/public").permitAll()
                
                // Endpoints públicos - Obtener estilistas para reservas (debe ir ANTES de /usuarios/**)
                .requestMatchers(HttpMethod.GET, "/usuarios/public/estilistas").permitAll()
                
                // Endpoints de usuarios - /usuarios/me debe ir ANTES de /usuarios/**
                .requestMatchers("/usuarios/me").authenticated()
                
                // Endpoints de usuarios - Solo ADMINISTRADOR para gestión completa (debe ir DESPUÉS de las excepciones)
                .requestMatchers("/usuarios/**").hasRole("ADMINISTRADOR")
                
                // Endpoints públicos - Obtener disponibilidades para reservas
                .requestMatchers(HttpMethod.GET, "/disponibilidades").permitAll()
                
                // Endpoints públicos - Obtener excepciones para reservas
                .requestMatchers(HttpMethod.GET, "/excepciones-horario").permitAll()
                
                // Endpoints públicos - Obtener citas filtradas por estilista/fecha (para ver disponibilidad)
                .requestMatchers(HttpMethod.GET, "/citas").permitAll()
                
                // Endpoints de citas - Autenticados pueden crear
                .requestMatchers(HttpMethod.POST, "/citas").hasAnyRole("CLIENTE", "ADMINISTRADOR")
                .requestMatchers("/citas/**").authenticated()
                
                // Endpoints públicos - Obtener servicios (para ver servicios disponibles al reservar)
                .requestMatchers(HttpMethod.GET, "/servicios").permitAll()
                .requestMatchers(HttpMethod.GET, "/servicios/**").permitAll()
                
                // Endpoints de servicios - ESTILISTA y ADMINISTRADOR pueden crear/actualizar/eliminar
                .requestMatchers(HttpMethod.POST, "/servicios").hasAnyRole("ESTILISTA", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT, "/servicios/**").hasAnyRole("ESTILISTA", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/servicios/**").hasAnyRole("ESTILISTA", "ADMINISTRADOR")
                
                // El resto requiere autenticación
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

