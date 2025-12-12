package com.bookmycut.dto;

import com.bookmycut.util.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * DTO for creating a new appointment.
 * Contains only the fields necessary for creation.
 */
@Schema(description = "DTO para crear una nueva cita")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateDTO {
    
    @Schema(description = "ID del cliente", example = "1", required = true)
    @NotNull(message = "El ID del cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser positivo")
    private Long clientId;
    
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
    
    @Schema(description = "Teléfono del cliente (opcional, formato: 9 dígitos)", example = "612345678")
    @ValidPhoneNumber(message = "El teléfono debe tener un formato válido (9 dígitos, opcional prefijo +34 o 0034)")
    private String clientPhone;
    
    @Schema(description = "Precio total de la cita (se calculará automáticamente si no se proporciona)", example = "50.00", required = false)
    private BigDecimal totalPrice; // Opcional: se calculará automáticamente
    
    @Schema(description = "Lista de IDs de servicios asociados a la cita", example = "[1, 2, 3]")
    private List<Long> serviceIds;
}

