package com.bookmycut.dto;

import com.bookmycut.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Respuesta de autenticación con token JWT e información del usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    @Schema(description = "Token JWT para autenticación", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Tipo de token", example = "Bearer")
    private String type = "Bearer";
    
    @Schema(description = "ID del usuario", example = "1")
    private Long userId;
    
    @Schema(description = "Nombre del usuario", example = "Juan Pérez")
    private String name;
    
    @Schema(description = "Email del usuario", example = "juan@example.com")
    private String email;
    
    @Schema(description = "Rol del usuario", example = "CLIENTE", allowableValues = {"CLIENTE", "ESTILISTA", "ADMINISTRADOR"})
    private User.Role role;
}

