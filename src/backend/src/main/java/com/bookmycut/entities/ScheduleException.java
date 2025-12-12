package com.bookmycut.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "excepciones_horario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleException {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "excep_horario_id")
    private Long scheduleExceptionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estilista_id")
    private User stylist; // Optional: null means for all stylists
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    @NotNull(message = "El administrador es obligatorio")
    private User administrator;
    
    @Column(nullable = false)
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;
    
    @Column(name = "hora_inicio")
    private LocalTime startTime; // Optional: null means full day
    
    @Column(name = "hora_fin")
    private LocalTime endTime; // Optional: null means full day
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "El tipo es obligatorio")
    private ExceptionType type;
    
    @Column(length = 500)
    private String reason;
    
    public enum ExceptionType {
        DISPONIBLE,
        NO_DISPONIBLE
    }
}
