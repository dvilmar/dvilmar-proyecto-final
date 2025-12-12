package com.bookmycut.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a notification in the system.
 */
@Entity
@Table(name = "notificaciones")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificacion_id")
    private Long notificationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "El usuario es obligatorio")
    private User user;
    
    @Column(nullable = false, length = 255)
    @NotNull(message = "El título es obligatorio")
    private String title;
    
    @Column(length = 500)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type = NotificationType.INFO;
    
    @Column(name = "is_read", nullable = false)
    private Boolean read = false;
    
    @Column(name = "related_appointment_id")
    private Long relatedAppointmentId;
    
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
        APPOINTMENT_REMINDER,
        APPOINTMENT_CANCELLED,
        APPOINTMENT_CONFIRMED
    }
}





