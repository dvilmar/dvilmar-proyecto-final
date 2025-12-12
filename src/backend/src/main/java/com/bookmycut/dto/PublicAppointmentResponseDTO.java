package com.bookmycut.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para creación de cita pública.
 * Incluye la cita creada y opcionalmente un token JWT si se creó un nuevo usuario con contraseña.
 */
@Schema(description = "Respuesta de creación de cita pública")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicAppointmentResponseDTO {
    
    @Schema(description = "Cita creada")
    private AppointmentDTO appointment;
    
    @Schema(description = "Token JWT (solo si se creó un nuevo usuario con contraseña)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Información de autenticación (solo si se proporcionó token)")
    private AuthResponse auth;
}




