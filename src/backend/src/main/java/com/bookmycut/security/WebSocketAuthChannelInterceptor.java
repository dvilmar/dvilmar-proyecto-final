package com.bookmycut.security;

import com.bookmycut.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new org.springframework.messaging.MessagingException("Falta el token de autenticación");
            }
            String jwt = authHeader.substring(7);

            String username = jwtUtil.extractUsername(jwt);
            if (username == null || !jwtUtil.validateToken(jwt, username)) {
                throw new org.springframework.messaging.MessagingException("Token de autenticación inválido");
            }

            Claims claims = jwtUtil.extractAllClaims(jwt);
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            List<SimpleGrantedAuthority> authorities = roles != null
                    ? roles.stream().map(SimpleGrantedAuthority::new).toList()
                    : List.of();

            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            accessor.setUser(authentication);
            accessor.getSessionAttributes().put("usuarioId", claims.get("usuarioId", Long.class));
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/topic/notifications/")) {
                Long subscribedUserId = Long.valueOf(destination.substring("/topic/notifications/".length()));
                Object sessionUserId = accessor.getSessionAttributes() != null
                        ? accessor.getSessionAttributes().get("usuarioId")
                        : null;
                if (!subscribedUserId.equals(sessionUserId)) {
                    throw new org.springframework.messaging.MessagingException(
                            "No autorizado para suscribirse a las notificaciones de otro usuario");
                }
            }
        }

        return message;
    }
}
