package com.bookmycut.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "disponibilidades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Availability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disp_id")
    private Long availabilityId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estilista_id", nullable = false)
    @NotNull(message = "El estilista es obligatorio")
    private User stylist;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 20)
    @NotNull(message = "El día de la semana es obligatorio")
    private DayOfWeek dayOfWeek;
    
    @Column(name = "hora_inicio", nullable = false)
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;
    
    @Column(name = "hora_fin", nullable = false)
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;
}
