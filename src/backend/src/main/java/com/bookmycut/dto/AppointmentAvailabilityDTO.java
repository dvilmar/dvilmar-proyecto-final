package com.bookmycut.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "DTO mínimo con el horario ocupado de una cita, para consultas de disponibilidad sin autenticación")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentAvailabilityDTO {
    @Schema(description = "ID del estilista", example = "2")
    private Long stylistId;

    @Schema(description = "Fecha de la cita", example = "2024-12-15")
    private LocalDate date;

    @Schema(description = "Hora de inicio", example = "10:00")
    private LocalTime startTime;

    @Schema(description = "Hora de fin", example = "11:00")
    private LocalTime endTime;

    @Schema(description = "Estado de la cita", example = "CONFIRMADA", allowableValues = {"CONFIRMADA", "CANCELADA", "FINALIZADA"})
    private String status;
}
