package com.bookmycut.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for transferring service offer information from server to client.
 */
@Schema(description = "DTO con información de un servicio ofrecido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOfferDTO {
    @Schema(description = "ID del servicio", example = "1")
    private Long serviceId;
    
    @Schema(description = "Nombre del servicio", example = "Corte de pelo", required = true)
    private String name;
    
    @Schema(description = "Descripción del servicio", example = "Corte de pelo profesional con lavado incluido")
    private String description;
    
    @Schema(description = "Duración del servicio en minutos", example = "30", required = true)
    private Integer duration;
    
    @Schema(description = "Precio unitario del servicio", example = "25.00", required = true)
    private BigDecimal unitPrice;
}
