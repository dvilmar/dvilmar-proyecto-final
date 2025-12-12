package com.bookmycut.service;

import com.bookmycut.dto.NotificationCreateDTO;
import com.bookmycut.dto.NotificationDTO;
import com.bookmycut.entities.Notification;
import com.bookmycut.entities.User;
import com.bookmycut.exception.ResourceNotFoundException;
import com.bookmycut.repositories.NotificationRepository;
import com.bookmycut.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios para NotificationService")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;
    private NotificationCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setName("Usuario Test");
        user.setEmail("usuario@test.com");

        notification = new Notification();
        notification.setNotificationId(1L);
        notification.setUser(user);
        notification.setTitle("Test Notification");
        notification.setMessage("Mensaje de prueba");
        notification.setType(Notification.NotificationType.APPOINTMENT_CONFIRMED);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());

        createDTO = new NotificationCreateDTO();
        createDTO.setUserId(1L);
        createDTO.setTitle("Test Notification");
        createDTO.setMessage("Mensaje de prueba");
        createDTO.setType(Notification.NotificationType.APPOINTMENT_CONFIRMED);
    }

    @Test
    @DisplayName("Debería obtener notificaciones paginadas de un usuario")
    void testGetUserNotifications() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> notificationPage = new PageImpl<>(List.of(notification), pageable, 1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserOrderByCreatedDateDesc(user, pageable))
                .thenReturn(notificationPage);

        // When
        Page<NotificationDTO> result = notificationService.getUserNotifications(1L, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería obtener notificaciones no leídas")
    void testGetUnreadNotifications() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.findUnreadNotifications(user))
                .thenReturn(List.of(notification));

        // When
        List<NotificationDTO> result = notificationService.getUnreadNotifications(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getRead());
    }

    @Test
    @DisplayName("Debería obtener conteo de notificaciones no leídas")
    void testGetUnreadCount() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.countByUserAndReadFalse(user)).thenReturn(3L);

        // When
        long result = notificationService.getUnreadCount(1L);

        // Then
        assertEquals(3L, result);
    }

    @Test
    @DisplayName("Debería crear una notificación")
    void testCreateNotification() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        NotificationDTO result = notificationService.createNotification(createDTO);

        // Then
        assertNotNull(result);
        assertEquals("Test Notification", result.getTitle());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Debería marcar notificación como leída")
    void testMarkAsRead() {
        // Given
        notification.setRead(false);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        // When
        NotificationDTO result = notificationService.markAsRead(1L, 1L);

        // Then
        assertNotNull(result);
        assertTrue(result.getRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("Debería verificar si existe recordatorio para una cita")
    void testHasReminderForAppointment() {
        // Given
        notification.setType(Notification.NotificationType.APPOINTMENT_REMINDER);
        notification.setRelatedAppointmentId(100L);
        when(notificationRepository.findByRelatedAppointmentIdAndType(
                100L, Notification.NotificationType.APPOINTMENT_REMINDER))
                .thenReturn(List.of(notification));

        // When
        boolean result = notificationService.hasReminderForAppointment(100L);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Debería eliminar una notificación")
    void testDeleteNotification() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        doNothing().when(notificationRepository).delete(notification);

        // When
        notificationService.deleteNotification(1L, 1L);

        // Then
        verify(notificationRepository).delete(notification);
    }

    @Test
    @DisplayName("Debería lanzar excepción si usuario no existe")
    void testCreateNotificationUserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        createDTO.setUserId(999L);
        assertThrows(ResourceNotFoundException.class, 
                () -> notificationService.createNotification(createDTO));
    }
}





