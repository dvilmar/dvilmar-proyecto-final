package com.bookmycut.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Schema(description = "DTO para crear una nueva disponibilidad")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityCreateDTO {
    @Schema(description = "ID del estilista", example = "2", required = true)
    @NotNull(message = "El ID del estilista es obligatorio")
    private Long stylistId;
    
    @Schema(description = "Día de la semana", example = "MONDAY", required = true)
    @NotNull(message = "El día de la semana es obligatorio")
    private DayOfWeek dayOfWeek;
    
    @Schema(description = "Hora de inicio", example = "09:00", required = true)
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;
    
    @Schema(description = "Hora de fin", example = "18:00", required = true)
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;
}





