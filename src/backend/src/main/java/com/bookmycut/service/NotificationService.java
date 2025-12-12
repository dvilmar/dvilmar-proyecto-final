package com.bookmycut.service;

import com.bookmycut.dto.NotificationCreateDTO;
import com.bookmycut.dto.NotificationDTO;
import com.bookmycut.entities.Appointment;
import com.bookmycut.entities.Notification;
import com.bookmycut.entities.User;
import com.bookmycut.exception.ResourceNotFoundException;
import com.bookmycut.repositories.NotificationRepository;
import com.bookmycut.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    public Page<NotificationDTO> getUserNotifications(Long userId, int page, int size) {
        logger.info("Requesting notifications for user ID: {}, page: {}, size: {}", userId, page, size);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
            Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedDateDesc(user, pageable);
            
            return notifications.map(this::toDTO);
        } catch (Exception e) {
            logger.error("Error getting notifications: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        logger.info("Requesting unread notifications for user ID: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            
            List<Notification> notifications = notificationRepository.findUnreadNotifications(user);
            return notifications.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting unread notifications: {}", e.getMessage(), e);
            throw e;
        }
    }

    public long getUnreadCount(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            return notificationRepository.countByUserAndReadFalse(user);
        } catch (Exception e) {
            logger.error("Error getting unread count: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Transactional
    public NotificationDTO createNotification(NotificationCreateDTO createDTO) {
        logger.info("Creating notification for user ID: {}", createDTO.getUserId());
        try {
            User user = userRepository.findById(createDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", createDTO.getUserId()));

            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(createDTO.getTitle());
            notification.setMessage(createDTO.getMessage());
            notification.setType(createDTO.getType());
            notification.setRead(false);
            notification.setRelatedAppointmentId(createDTO.getRelatedAppointmentId());

            Notification saved = notificationRepository.save(notification);
            NotificationDTO dto = toDTO(saved);

            // Enviar notificación por WebSocket
            if (messagingTemplate != null) {
                messagingTemplate.convertAndSend("/topic/notifications/" + user.getUserId(), dto);
            }

            logger.info("Notification created successfully with ID: {}", saved.getNotificationId());
            return dto;
        } catch (Exception e) {
            logger.error("Error creating notification: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Async
    public void sendAppointmentReminder(Appointment appointment) {
        try {
            LocalDateTime reminderTime = LocalDateTime.of(appointment.getDate(), appointment.getStartTime()).minusHours(24);
            if (reminderTime.isAfter(LocalDateTime.now())) {
                // Crear notificación para el cliente
                NotificationCreateDTO clientNotification = new NotificationCreateDTO();
                clientNotification.setUserId(appointment.getClient().getUserId());
                clientNotification.setTitle("Recordatorio de cita");
                clientNotification.setMessage(String.format(
                    "Tienes una cita mañana %s a las %s con %s",
                    appointment.getDate(),
                    appointment.getStartTime(),
                    appointment.getStylist().getName()
                ));
                clientNotification.setType(Notification.NotificationType.APPOINTMENT_REMINDER);
                clientNotification.setRelatedAppointmentId(appointment.getAppointmentId());
                createNotification(clientNotification);

                // Crear notificación para el estilista
                NotificationCreateDTO stylistNotification = new NotificationCreateDTO();
                stylistNotification.setUserId(appointment.getStylist().getUserId());
                stylistNotification.setTitle("Recordatorio de cita");
                stylistNotification.setMessage(String.format(
                    "Tienes una cita mañana %s a las %s con %s",
                    appointment.getDate(),
                    appointment.getStartTime(),
                    appointment.getClient().getName()
                ));
                stylistNotification.setType(Notification.NotificationType.APPOINTMENT_REMINDER);
                stylistNotification.setRelatedAppointmentId(appointment.getAppointmentId());
                createNotification(stylistNotification);
            }
        } catch (Exception e) {
            logger.error("Error sending appointment reminder: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        logger.info("Marking notification ID: {} as read for user ID: {}", notificationId, userId);
        try {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

            if (!notification.getUser().getUserId().equals(userId)) {
                throw new ResourceNotFoundException("Notification", "id", notificationId);
            }

            notification.setRead(true);
            Notification updated = notificationRepository.save(notification);
            logger.info("Notification marked as read");
            return toDTO(updated);
        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        logger.info("Marking all notifications as read for user ID: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            
            List<Notification> unread = notificationRepository.findUnreadNotifications(user);
            unread.forEach(n -> n.setRead(true));
            notificationRepository.saveAll(unread);
            logger.info("All notifications marked as read");
        } catch (Exception e) {
            logger.error("Error marking all notifications as read: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        logger.info("Deleting notification ID: {} for user ID: {}", notificationId, userId);
        try {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

            if (!notification.getUser().getUserId().equals(userId)) {
                throw new ResourceNotFoundException("Notification", "id", notificationId);
            }

            notificationRepository.delete(notification);
            logger.info("Notification deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting notification: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Verifica si ya existe un recordatorio para una cita específica.
     */
    public boolean hasReminderForAppointment(Long appointmentId) {
        try {
            List<Notification> reminders = notificationRepository
                    .findByRelatedAppointmentIdAndType(
                            appointmentId, 
                            Notification.NotificationType.APPOINTMENT_REMINDER
                    );
            return !reminders.isEmpty();
        } catch (Exception e) {
            logger.error("Error checking reminder for appointment {}: {}", appointmentId, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si ya se envió un recordatorio hoy para una cita específica.
     * Útil para el scheduler que se ejecuta diariamente.
     */
    public boolean hasReminderForAppointmentToday(Long appointmentId) {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);
            
            List<Notification> reminders = notificationRepository
                    .findByRelatedAppointmentIdAndType(
                            appointmentId, 
                            Notification.NotificationType.APPOINTMENT_REMINDER
                    );
            
            // Filtrar solo las notificaciones de hoy
            return reminders.stream()
                    .anyMatch(n -> {
                        LocalDateTime created = n.getCreatedDate();
                        return created != null && 
                               !created.isBefore(startOfDay) && 
                               !created.isAfter(endOfDay);
                    });
        } catch (Exception e) {
            logger.error("Error checking reminder for appointment {} today: {}", appointmentId, e.getMessage());
            return false;
        }
    }

    private NotificationDTO toDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(notification.getNotificationId());
        dto.setUserId(notification.getUser().getUserId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.getRead());
        dto.setRelatedAppointmentId(notification.getRelatedAppointmentId());
        dto.setCreatedDate(notification.getCreatedDate());
        return dto;
    }
}





