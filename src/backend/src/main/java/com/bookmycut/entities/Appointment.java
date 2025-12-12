package com.bookmycut.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an appointment in the system.
 * Includes audit fields to track creation and modification dates.
 */
@Entity
@Table(name = "citas")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cita_id")
    private Long appointmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @NotNull(message = "El cliente es obligatorio")
    private User client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estilista_id", nullable = false)
    @NotNull(message = "El estilista es obligatorio")
    private User stylist;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.CONFIRMADA;
    
    @Column(nullable = false)
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;
    
    @Column(name = "hora_inicio", nullable = false)
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;
    
    @Column(name = "hora_fin", nullable = false)
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;
    
    @Column(name = "telefono_cliente", length = 20)
    private String clientPhone;
    
    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El precio total es obligatorio")
    private BigDecimal totalPrice;
    
    @ManyToMany
    @JoinTable(
        name = "cita_servicio",
        joinColumns = @JoinColumn(name = "cita_id"),
        inverseJoinColumns = @JoinColumn(name = "servicio_id")
    )
    private List<ServiceOffer> services = new ArrayList<>();
    
    // Audit fields
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
    
    public enum AppointmentStatus {
        CONFIRMADA,
        CANCELADA,
        FINALIZADA
    }
}
