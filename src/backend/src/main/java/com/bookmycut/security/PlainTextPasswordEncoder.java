package com.bookmycut.security;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoder que usa contraseñas en texto plano (sin encriptar).
 * SOLO PARA DESARROLLO - NO USAR EN PRODUCCIÓN
 */
public class PlainTextPasswordEncoder implements PasswordEncoder {
    
    @Override
    public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
    }
    
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return rawPassword.toString().equals(encodedPassword);
    }
}

