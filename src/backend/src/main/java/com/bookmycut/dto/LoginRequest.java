package com.bookmycut.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para autenticación de usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @Schema(description = "Username del usuario", example = "admin", required = true)
    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;
    
    @Schema(description = "Contraseña del usuario", example = "password123", required = true)
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}

