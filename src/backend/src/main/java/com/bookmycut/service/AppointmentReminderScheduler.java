package com.bookmycut.service;

import com.bookmycut.entities.Appointment;
import com.bookmycut.entities.Notification;
import com.bookmycut.repositories.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio programado para enviar recordatorios automáticos de citas.
 * Ejecuta diariamente a las 9:00 AM y envía recordatorios para citas que ocurren en las próximas 24 horas.
 */
@Component
public class AppointmentReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentReminderScheduler.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Envía recordatorios automáticos de citas.
     * Se ejecuta diariamente a las 9:00 AM.
     * Envía recordatorios para citas confirmadas que ocurren mañana.
     */
    @Scheduled(cron = "0 0 9 * * *") // Diariamente a las 9:00 AM
    @Transactional
    public void sendAppointmentReminders() {
        logger.info("Iniciando envío de recordatorios automáticos de citas...");
        
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            
            // Buscar todas las citas confirmadas de mañana
            List<Appointment> appointments = appointmentRepository
                    .findByDateAndStatusOrderByStartTime(tomorrow, Appointment.AppointmentStatus.CONFIRMADA);
            
            logger.info("Encontradas {} citas para mañana", appointments.size());
            
            int remindersSent = 0;
            for (Appointment appointment : appointments) {
                try {
                    // Verificar que no se haya enviado ya un recordatorio de recordatorio para esta cita
                    if (!hasReminderBeenSent(appointment)) {
                        sendReminderForAppointment(appointment);
                        remindersSent++;
                    }
                } catch (Exception e) {
                    logger.error("Error enviando recordatorio para cita ID {}: {}", 
                            appointment.getAppointmentId(), e.getMessage(), e);
                }
            }
            
            logger.info("Recordatorios enviados: {}/{}", remindersSent, appointments.size());
        } catch (Exception e) {
            logger.error("Error en el scheduler de recordatorios: {}", e.getMessage(), e);
        }
    }

    /**
     * Verifica si ya se envió un recordatorio para esta cita.
     * Solo verifica recordatorios enviados hoy para evitar duplicados.
     */
    private boolean hasReminderBeenSent(Appointment appointment) {
        // Verificar si ya existe una notificación de tipo APPOINTMENT_REMINDER para esta cita hoy
        // Esto evita enviar múltiples recordatorios en la misma ejecución del scheduler
        return notificationService.hasReminderForAppointmentToday(appointment.getAppointmentId());
    }

    /**
     * Envía recordatorios al cliente y al estilista para una cita.
     */
    private void sendReminderForAppointment(Appointment appointment) {
        logger.info("Enviando recordatorio para cita ID: {} - Cliente: {}, Estilista: {}", 
                appointment.getAppointmentId(), 
                appointment.getClient().getName(), 
                appointment.getStylist().getName());

        // Crear notificación para el cliente
        com.bookmycut.dto.NotificationCreateDTO clientNotification = 
                new com.bookmycut.dto.NotificationCreateDTO();
        clientNotification.setUserId(appointment.getClient().getUserId());
        clientNotification.setTitle("Recordatorio de cita mañana");
        String fechaRecordatorioCliente = appointment.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        clientNotification.setMessage(String.format(
            "Recordatorio: Tienes una cita mañana (%s) a las %s con %s. Servicios: %s",
            fechaRecordatorioCliente,
            appointment.getStartTime(),
            appointment.getStylist().getName(),
            appointment.getServices().stream()
                    .map(s -> s.getName())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("N/A")
        ));
        clientNotification.setType(Notification.NotificationType.APPOINTMENT_REMINDER);
        clientNotification.setRelatedAppointmentId(appointment.getAppointmentId());
        
        notificationService.createNotification(clientNotification);

        // Crear notificación para el estilista
        com.bookmycut.dto.NotificationCreateDTO stylistNotification = 
                new com.bookmycut.dto.NotificationCreateDTO();
        stylistNotification.setUserId(appointment.getStylist().getUserId());
        stylistNotification.setTitle("Recordatorio de cita mañana");
        String fechaRecordatorioEstilista = appointment.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        stylistNotification.setMessage(String.format(
            "Recordatorio: Tienes una cita mañana (%s) a las %s con %s. Servicios: %s",
            fechaRecordatorioEstilista,
            appointment.getStartTime(),
            appointment.getClient().getName(),
            appointment.getServices().stream()
                    .map(s -> s.getName())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("N/A")
        ));
        stylistNotification.setType(Notification.NotificationType.APPOINTMENT_REMINDER);
        stylistNotification.setRelatedAppointmentId(appointment.getAppointmentId());
        
        notificationService.createNotification(stylistNotification);
    }
}





