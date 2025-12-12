package com.bookmycut.repositories;

import com.bookmycut.entities.Appointment;
import com.bookmycut.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClient(User client);
    Page<Appointment> findByClient(User client, Pageable pageable);
    List<Appointment> findByStylist(User stylist);
    Page<Appointment> findByStylist(User stylist, Pageable pageable);
    List<Appointment> findByDate(LocalDate date);
    List<Appointment> findByClientAndDateAfter(User client, LocalDate date);
    List<Appointment> findByStylistAndDate(User stylist, LocalDate date);
    
    @Query("SELECT a FROM Appointment a WHERE a.stylist = :stylist AND a.date BETWEEN :startDate AND :endDate")
    List<Appointment> findByStylistAndDateBetween(
        @Param("stylist") User stylist,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Encuentra citas que se solapan con el horario especificado para un estilista.
     * Una cita se solapa si:
     * - Está en la misma fecha
     * - El estilista es el mismo
     * - Las horas se solapan (la nueva cita comienza antes de que termine la existente Y termina después de que comience la existente)
     * - El estado NO es CANCELADA
     */
    @Query("SELECT a FROM Appointment a WHERE a.stylist = :stylist " +
           "AND a.date = :date " +
           "AND a.status != 'CANCELADA' " +
           "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    List<Appointment> findOverlappingAppointments(
        @Param("stylist") User stylist,
        @Param("date") LocalDate date,
        @Param("startTime") java.time.LocalTime startTime,
        @Param("endTime") java.time.LocalTime endTime
    );
    
    /**
     * Encuentra citas solapadas excluyendo una cita específica (útil para actualizaciones).
     */
    @Query("SELECT a FROM Appointment a WHERE a.stylist = :stylist " +
           "AND a.date = :date " +
           "AND a.appointmentId != :excludeAppointmentId " +
           "AND a.status != 'CANCELADA' " +
           "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    List<Appointment> findOverlappingAppointmentsExcluding(
        @Param("stylist") User stylist,
        @Param("date") LocalDate date,
        @Param("startTime") java.time.LocalTime startTime,
        @Param("endTime") java.time.LocalTime endTime,
        @Param("excludeAppointmentId") Long excludeAppointmentId
    );
    
    List<Appointment> findByDateAndStatusOrderByStartTime(LocalDate date, Appointment.AppointmentStatus status);
    
    /**
     * Busca citas por nombre del cliente (case-insensitive, contiene).
     */
    @Query("SELECT a FROM Appointment a WHERE LOWER(a.client.name) LIKE LOWER(CONCAT('%', :clientName, '%'))")
    List<Appointment> findByClientNameContainingIgnoreCase(@Param("clientName") String clientName);
    
    /**
     * Busca citas por nombre del estilista (case-insensitive, contiene).
     */
    @Query("SELECT a FROM Appointment a WHERE LOWER(a.stylist.name) LIKE LOWER(CONCAT('%', :stylistName, '%'))")
    List<Appointment> findByStylistNameContainingIgnoreCase(@Param("stylistName") String stylistName);
    
    /**
     * Busca citas por nombre de servicio (case-insensitive, contiene).
     */
    @Query("SELECT DISTINCT a FROM Appointment a JOIN a.services s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :serviceName, '%'))")
    List<Appointment> findByServiceNameContainingIgnoreCase(@Param("serviceName") String serviceName);
    
    /**
     * Busca citas con múltiples criterios (paginado).
     */
    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN a.services s " +
           "WHERE (:clientName IS NULL OR LOWER(a.client.name) LIKE LOWER(CONCAT('%', :clientName, '%'))) " +
           "AND (:stylistName IS NULL OR LOWER(a.stylist.name) LIKE LOWER(CONCAT('%', :stylistName, '%'))) " +
           "AND (:serviceName IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :serviceName, '%'))) " +
           "AND (:date IS NULL OR a.date = :date) " +
           "AND (:status IS NULL OR a.status = :status)")
    Page<Appointment> findWithAdvancedFilters(
        @Param("clientName") String clientName,
        @Param("stylistName") String stylistName,
        @Param("serviceName") String serviceName,
        @Param("date") LocalDate date,
        @Param("status") Appointment.AppointmentStatus status,
        Pageable pageable
    );
    
    /**
     * Busca citas con múltiples criterios (sin paginación).
     */
    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN a.services s " +
           "WHERE (:clientName IS NULL OR LOWER(a.client.name) LIKE LOWER(CONCAT('%', :clientName, '%'))) " +
           "AND (:stylistName IS NULL OR LOWER(a.stylist.name) LIKE LOWER(CONCAT('%', :stylistName, '%'))) " +
           "AND (:serviceName IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :serviceName, '%'))) " +
           "AND (:date IS NULL OR a.date = :date) " +
           "AND (:status IS NULL OR a.status = :status)")
    List<Appointment> findWithAdvancedFilters(
        @Param("clientName") String clientName,
        @Param("stylistName") String stylistName,
        @Param("serviceName") String serviceName,
        @Param("date") LocalDate date,
        @Param("status") Appointment.AppointmentStatus status
    );
}

