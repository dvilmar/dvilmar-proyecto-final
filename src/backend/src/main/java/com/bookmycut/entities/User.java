package com.bookmycut.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a user in the system.
 * Uses a single table with a role field instead of inheritance.
 * Includes audit fields to track creation and modification dates.
 */
@Entity
@Table(name = "usuarios")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long userId;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    @Column(name = "username", unique = true, length = 50)
    private String username; // Requerido para ESTILISTA y ADMINISTRADOR, opcional para CLIENTE
    
    @Email(message = "El email debe tener un formato válido")
    @Column(nullable = true, unique = true, length = 100)
    private String email; // Opcional, solo para clientes
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(length = 20)
    private String phone; // Only for clients, but can be null for other roles
    
    // Relationships
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> clientAppointments = new ArrayList<>();
    
    @OneToMany(mappedBy = "stylist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> stylistAppointments = new ArrayList<>();
    
    @OneToMany(mappedBy = "stylist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Availability> availabilities = new ArrayList<>();
    
    @OneToMany(mappedBy = "stylist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleException> scheduleExceptions = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "estilista_servicio",
        joinColumns = @JoinColumn(name = "estilista_id"),
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
    
    @Column(name = "last_password_change_date")
    private LocalDateTime lastPasswordChangeDate;
    
    /**
     * Sets the user's password and updates the last password change date.
     *
     * @param password New user password.
     */
    public void setPassword(String password) {
        this.password = password;
        this.lastPasswordChangeDate = LocalDateTime.now();
    }
    
    public enum Role {
        CLIENTE,
        ESTILISTA,
        ADMINISTRADOR
    }
}
