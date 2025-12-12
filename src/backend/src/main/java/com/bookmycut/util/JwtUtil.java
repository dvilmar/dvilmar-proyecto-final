package com.bookmycut.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // JWT requiere al menos 256 bits (32 bytes) para HS256
        // Si la clave es más corta, repetirla hasta alcanzar 32 bytes
        if (keyBytes.length < 32) {
            byte[] expandedKey = new byte[32];
            for (int i = 0; i < 32; i++) {
                expandedKey[i] = keyBytes[i % keyBytes.length];
            }
            keyBytes = expandedKey;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extrae todos los claims del token.
     *
     * @param token Token JWT.
     * @return Claims del token.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Genera un token JWT con los roles del usuario.
     *
     * @param email Email del usuario (subject del token).
     * @param usuarioId ID del usuario.
     * @param roles Lista de roles del usuario.
     * @return Token JWT generado.
     */
    public String generateToken(String email, Long usuarioId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("usuarioId", usuarioId);
        claims.put("roles", roles);
        return createToken(claims, email);
    }
    
    /**
     * Genera un token JWT con un solo rol (método de compatibilidad).
     *
     * @param email Email del usuario.
     * @param usuarioId ID del usuario.
     * @param rol Rol del usuario.
     * @return Token JWT generado.
     */
    public String generateToken(String email, Long usuarioId, String rol) {
        return generateToken(email, usuarioId, List.of("ROLE_" + rol));
    }
    
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public Boolean validateToken(String token, String email) {
        final String username = extractUsername(token);
        return (username.equals(email) && !isTokenExpired(token));
    }
}

