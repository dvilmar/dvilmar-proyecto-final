package com.bookmycut.dto;

import com.bookmycut.entities.ScheduleException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "DTO con información de una excepción de horario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleExceptionDTO {
    @Schema(description = "ID de la excepción", example = "1")
    private Long scheduleExceptionId;
    
    @Schema(description = "ID del estilista (null si es para todos)", example = "2")
    private Long stylistId;
    
    @Schema(description = "Nombre del estilista")
    private String stylistName;
    
    @Schema(description = "ID del administrador", example = "1", required = true)
    private Long administratorId;
    
    @Schema(description = "Nombre del administrador", example = "Admin Principal")
    private String administratorName;
    
    @Schema(description = "Fecha de la excepción", example = "2024-12-25", required = true)
    private LocalDate date;
    
    @Schema(description = "Hora de inicio (null si es todo el día)", example = "10:00")
    private LocalTime startTime;
    
    @Schema(description = "Hora de fin (null si es todo el día)", example = "14:00")
    private LocalTime endTime;
    
    @Schema(description = "Tipo de excepción", example = "NO_DISPONIBLE", required = true)
    private ScheduleException.ExceptionType type;
    
    @Schema(description = "Razón de la excepción", example = "Día festivo")
    private String reason;
}





