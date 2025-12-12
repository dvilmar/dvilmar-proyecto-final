package com.bookmycut.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a service offered by the salon.
 * Includes audit fields to track creation and modification dates.
 */
@Entity
@Table(name = "servicios")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOffer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "servicio_id")
    private Long serviceId;
    
    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @NotNull(message = "La duración es obligatoria")
    @Positive(message = "La duración debe ser positiva")
    @Column(nullable = false)
    private Integer duration; // in minutes
    
    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @ManyToMany(mappedBy = "services")
    private List<User> stylists = new ArrayList<>();
    
    @ManyToMany(mappedBy = "services")
    private List<Appointment> appointments = new ArrayList<>();
    
    // Audit fields
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
}
