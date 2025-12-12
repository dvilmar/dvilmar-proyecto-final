package com.bookmycut.dto;

import com.bookmycut.entities.User;
import com.bookmycut.util.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para registro de nuevos usuarios en el sistema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez", required = true, minLength = 2, maxLength = 100)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;
    
    @Schema(description = "Username para login (requerido para ESTILISTA y ADMINISTRADOR, opcional para CLIENTE)", example = "juan.perez")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;
    
    @Schema(description = "Email del usuario (opcional, solo para clientes)", example = "juan@example.com")
    @Email(message = "El email debe tener un formato válido")
    private String email;
    
    @Schema(description = "Contraseña del usuario (mínimo 6 caracteres)", example = "password123", required = true, minLength = 6)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    @Schema(description = "Rol del usuario", example = "CLIENTE", required = true, allowableValues = {"CLIENTE", "ESTILISTA", "ADMINISTRADOR"})
    @NotNull(message = "El rol es obligatorio")
    private User.Role role;
    
    @Schema(description = "Teléfono del usuario (solo requerido para clientes, formato: 9 dígitos)", example = "612345678")
    @ValidPhoneNumber(message = "El teléfono debe tener un formato válido (9 dígitos, opcional prefijo +34 o 0034)")
    private String phone; // Only for clients
}

