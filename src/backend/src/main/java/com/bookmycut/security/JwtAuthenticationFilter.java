package com.bookmycut.security;

import com.bookmycut.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de autenticación JWT que intercepta cada solicitud HTTP entrante
 * y valida el token JWT si está presente en el encabezado de autorización.
 * Extrae los roles del token y los establece en el contexto de seguridad.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    /**
     * Método principal del filtro que intercepta cada solicitud HTTP entrante
     * y valida el token JWT si está presente en el encabezado de autorización.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. Extraer el encabezado Authorization de la solicitud
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        
        // 2. Verificar si el encabezado Authorization está presente y tiene un token válido
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Si el encabezado no está presente o no comienza con "Bearer ", pasa la solicitud al siguiente filtro
            filterChain.doFilter(request, response);
            return;
        }
        
        // 3. Extraer el token JWT del encabezado (sin el prefijo "Bearer ")
        jwt = authHeader.substring(7);
        
        try {
            // 4. Extraer el nombre de usuario (claim "sub") del token
            username = jwtUtil.extractUsername(jwt);
            
            // 5. Verificar si:
            // - El nombre de usuario extraído no es nulo
            // - No hay una autenticación existente en el contexto de seguridad
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 6. Cargar los detalles del usuario desde el servicio personalizado
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 7. Validar el token JWT con el nombre de usuario del usuario cargado
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    // 8. Extraer los claims del token (como los roles)
                    Claims claims = jwtUtil.extractAllClaims(jwt);
                    
                    // 9. Extraer los roles del claim "roles" y convertirlos en GrantedAuthority
                    @SuppressWarnings("unchecked")
                    List<String> roles = claims.get("roles", List.class);
                    
                    List<SimpleGrantedAuthority> authorities;
                    if (roles != null && !roles.isEmpty()) {
                        // Usar los roles del token
                        authorities = roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList();
                    } else {
                        // Fallback a los roles del UserDetails
                        authorities = userDetails.getAuthorities().stream()
                                .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                                .toList();
                    }
                    
                    // 10. Crear un objeto UsernamePasswordAuthenticationToken con los detalles del usuario y sus roles
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);
                    
                    // 11. Configurar los detalles adicionales de la solicitud actual
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 12. Establecer la autenticación en el contexto de seguridad de Spring
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Usuario autenticado: {} con roles: {}", username, roles);
                } else {
                    logger.warn("Token JWT no válido para usuario: {}", username);
                }
            } else {
                logger.debug("Username es null o ya existe autenticación en el contexto para request: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            logger.error("Error al procesar el token JWT para request {}: {}", request.getRequestURI(), e.getMessage(), e);
            // Continuar con el filtro aunque haya error (dejar que SecurityConfig maneje la autorización)
        }
        
        // 13. Continuar con el siguiente filtro en la cadena de filtros
        filterChain.doFilter(request, response);
    }
}

