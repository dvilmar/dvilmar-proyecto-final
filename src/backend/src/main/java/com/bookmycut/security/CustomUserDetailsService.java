package com.bookmycut.security;

import com.bookmycut.entities.User;
import com.bookmycut.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar primero por username, si no se encuentra, buscar por email
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con username o email: " + username));
        
        if (!user.getActive()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }
        
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        
        // El username del UserDetails debe ser el identificador usado para autenticación
        // Si el usuario tiene username, usarlo; si no, usar email
        // Esto permite que el login funcione tanto con username como con email
        String userDetailsUsername = user.getUsername();
        if (userDetailsUsername == null || userDetailsUsername.trim().isEmpty()) {
            userDetailsUsername = user.getEmail();
        }
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(userDetailsUsername)
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getActive())
                .build();
    }
}

