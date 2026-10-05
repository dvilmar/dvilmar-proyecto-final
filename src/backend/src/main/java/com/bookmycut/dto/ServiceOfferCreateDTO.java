package com.bookmycut.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating a new service offer.
 * Contains only the fields necessary for creation.
 */
@Schema(description = "DTO para crear un nuevo servicio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOfferCreateDTO {
    
    @Schema(description = "Nombre del servicio", example = "Corte de pelo", required = true, maxLength = 100)
    @NotBlank(message = "El nombre del servicio es obligatorio")
    private String name;
    
    @Schema(description = "Descripción del servicio", example = "Corte de pelo profesional con lavado incluido", maxLength = 500)
    private String description;
    
    @Schema(description = "Duración del servicio en minutos", example = "30", required = true)
    @NotNull(message = "La duración es obligatoria")
    @Positive(message = "La duración debe ser positiva")
    private Integer duration;
    
    @Schema(description = "Precio unitario del servicio", example = "25.00", required = true)
    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal unitPrice;
}
