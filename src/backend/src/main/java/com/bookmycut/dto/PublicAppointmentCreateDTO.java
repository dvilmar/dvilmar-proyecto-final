package com.bookmycut.dto;

import com.bookmycut.util.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO for creating an appointment without authentication.
 * Contains client information that will be used to find or create a CLIENTE user.
 */
@Schema(description = "DTO para crear una cita pública (sin autenticación)")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicAppointmentCreateDTO {
    
    @Schema(description = "Nombre del cliente", example = "Juan Pérez", required = true)
    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String clientName;
    
    @Schema(description = "Email del cliente", example = "cliente@example.com", required = true)
    @NotBlank(message = "El email del cliente es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String clientEmail;
    
    @Schema(description = "Teléfono del cliente", example = "612345678", required = true)
    @NotBlank(message = "El teléfono del cliente es obligatorio")
    @ValidPhoneNumber(message = "El teléfono debe tener un formato válido (9 dígitos, opcional prefijo +34 o 0034)")
    private String clientPhone;
    
    @Schema(description = "Contraseña del cliente (mínimo 6 caracteres). Si se proporciona, se creará una cuenta con acceso por email.", example = "password123", required = false)
    private String clientPassword;
    
    @Schema(description = "ID del estilista", example = "2", required = true)
    @NotNull(message = "El ID del estilista es obligatorio")
    @Positive(message = "El ID del estilista debe ser positivo")
    private Long stylistId;
    
    @Schema(description = "Fecha de la cita (formato: YYYY-MM-DD)", example = "2024-12-15", required = true)
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;
    
    @Schema(description = "Hora de inicio (formato: HH:mm)", example = "10:00", required = true)
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;
    
    @Schema(description = "Hora de fin (formato: HH:mm)", example = "11:00", required = true)
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;
    
    @Schema(description = "Precio total de la cita (se calculará automáticamente si no se proporciona)", example = "50.00", required = false)
    private BigDecimal totalPrice; // Opcional: se calculará automáticamente
    
    @Schema(description = "Lista de IDs de servicios asociados a la cita", example = "[1, 2, 3]")
    private List<Long> serviceIds;
}




