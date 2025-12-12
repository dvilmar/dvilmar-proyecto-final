package com.bookmycut.controller;

import com.bookmycut.dto.NotificationDTO;
import com.bookmycut.entities.User;
import com.bookmycut.repositories.UserRepository;
import com.bookmycut.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Notifications", description = "Endpoints para gestión de notificaciones")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Obtener notificaciones del usuario (paginadas)")
    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            Authentication authentication,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Requesting notifications - page: {}, size: {}", page, size);
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Authentication is null or not authenticated");
                throw new RuntimeException("Usuario no autenticado");
            }
            String username = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            Page<NotificationDTO> notifications = notificationService.getUserNotifications(user.getUserId(), page, size);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error getting notifications: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Obtener notificaciones no leídas")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Authentication authentication) {
        logger.info("Requesting unread notifications");
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Authentication is null or not authenticated");
                throw new RuntimeException("Usuario no autenticado");
            }
            String username = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            List<NotificationDTO> notifications = notificationService.getUnreadNotifications(user.getUserId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error getting unread notifications: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Obtener contador de notificaciones no leídas")
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        logger.info("Requesting unread count - Authentication: {}", authentication != null ? authentication.getName() : "null");
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Authentication is null or not authenticated for unread count request");
                // Devolver 0 si no hay autenticación, pero debería ser manejado por Spring Security
                return ResponseEntity.ok(0L);
            }
            String username = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            long count = notificationService.getUnreadCount(user.getUserId());
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error getting unread count: {}", e.getMessage(), e);
            return ResponseEntity.ok(0L);
        }
    }

    @Operation(summary = "Marcar notificación como leída")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("Marking notification as read - ID: {}", id);
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Authentication is null or not authenticated");
                throw new RuntimeException("Usuario no autenticado");
            }
            String username = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            NotificationDTO notification = notificationService.markAsRead(id, user.getUserId());
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Marcar todas las notificaciones como leídas")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        logger.info("Marking all notifications as read");
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            notificationService.markAllAsRead(user.getUserId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error marking all notifications as read: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Eliminar notificación")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("Deleting notification - ID: {}", id);
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Authentication is null or not authenticated");
                throw new RuntimeException("Usuario no autenticado");
            }
            String username = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            notificationService.deleteNotification(id, user.getUserId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting notification: {}", e.getMessage(), e);
            throw e;
        }
    }
}





