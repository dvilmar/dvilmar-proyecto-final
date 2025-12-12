package com.bookmycut.dto;

import com.bookmycut.entities.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para crear una nueva notificación")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateDTO {
    @Schema(description = "ID del usuario", example = "1", required = true)
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId;
    
    @Schema(description = "Título de la notificación", example = "Recordatorio de cita", required = true)
    @NotNull(message = "El título es obligatorio")
    private String title;
    
    @Schema(description = "Mensaje de la notificación", example = "Tienes una cita mañana a las 10:00")
    private String message;
    
    @Schema(description = "Tipo de notificación", example = "APPOINTMENT_REMINDER", required = true)
    @NotNull(message = "El tipo es obligatorio")
    private Notification.NotificationType type;
    
    @Schema(description = "ID de la cita relacionada (opcional)", example = "5")
    private Long relatedAppointmentId;
}





