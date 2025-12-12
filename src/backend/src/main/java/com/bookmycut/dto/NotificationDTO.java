package com.bookmycut.dto;

import com.bookmycut.entities.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "DTO con información de una notificación")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    @Schema(description = "ID de la notificación", example = "1")
    private Long notificationId;
    
    @Schema(description = "ID del usuario", example = "1", required = true)
    private Long userId;
    
    @Schema(description = "Título de la notificación", example = "Recordatorio de cita", required = true)
    private String title;
    
    @Schema(description = "Mensaje de la notificación", example = "Tienes una cita mañana a las 10:00")
    private String message;
    
    @Schema(description = "Tipo de notificación", example = "APPOINTMENT_REMINDER", required = true)
    private Notification.NotificationType type;
    
    @Schema(description = "Indica si la notificación ha sido leída", example = "false")
    private Boolean read;
    
    @Schema(description = "ID de la cita relacionada (opcional)", example = "5")
    private Long relatedAppointmentId;
    
    @Schema(description = "Fecha de creación", example = "2024-12-20T10:00:00")
    private LocalDateTime createdDate;
}





