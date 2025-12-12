package com.bookmycut.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Schema(description = "DTO con información de disponibilidad de un estilista")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDTO {
    @Schema(description = "ID de la disponibilidad", example = "1")
    private Long availabilityId;
    
    @Schema(description = "ID del estilista", example = "2", required = true)
    private Long stylistId;
    
    @Schema(description = "Nombre del estilista", example = "Juan Pérez")
    private String stylistName;
    
    @Schema(description = "Día de la semana", example = "MONDAY", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private DayOfWeek dayOfWeek;
    
    @Schema(description = "Hora de inicio", example = "09:00", required = true)
    private LocalTime startTime;
    
    @Schema(description = "Hora de fin", example = "18:00", required = true)
    private LocalTime endTime;
}





