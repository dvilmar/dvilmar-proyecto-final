package com.bookmycut.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO for transferring appointment information from server to client.
 * Includes complete appointment information without exposing internal entity details.
 */
@Schema(description = "DTO con información completa de una cita")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {
    @Schema(description = "ID de la cita", example = "1")
    private Long appointmentId;
    
    @Schema(description = "ID del cliente", example = "1")
    private Long clientId;
    
    @Schema(description = "Nombre del cliente", example = "Juan Pérez")
    private String clientName;
    
    @Schema(description = "ID del estilista", example = "2")
    private Long stylistId;
    
    @Schema(description = "Nombre del estilista", example = "María García")
    private String stylistName;
    
    @Schema(description = "Estado de la cita", example = "CONFIRMADA", allowableValues = {"CONFIRMADA", "CANCELADA", "FINALIZADA"})
    private String status;
    
    @Schema(description = "Fecha de la cita", example = "2024-12-15")
    private LocalDate date;
    
    @Schema(description = "Hora de inicio", example = "10:00")
    private LocalTime startTime;
    
    @Schema(description = "Hora de fin", example = "11:00")
    private LocalTime endTime;
    
    @Schema(description = "Teléfono del cliente", example = "123456789")
    private String clientPhone;
    
    @Schema(description = "Precio total de la cita", example = "50.00")
    private BigDecimal totalPrice;
    
    @Schema(description = "Lista de servicios asociados a la cita")
    private List<ServiceOfferDTO> services;

    /**
     * Internal DTO for representing a service within an appointment.
     */
    @Schema(description = "Servicio dentro de una cita")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceOfferDTO {
        @Schema(description = "ID del servicio", example = "1")
        private Long serviceId;
        
        @Schema(description = "Nombre del servicio", example = "Corte de pelo")
        private String name;
        
        @Schema(description = "Precio unitario del servicio", example = "25.00")
        private BigDecimal unitPrice;
        
        @Schema(description = "Duración del servicio en minutos", example = "30")
        private Integer duration;
    }
}
