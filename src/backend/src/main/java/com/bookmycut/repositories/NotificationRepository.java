package com.bookmycut.repositories;

import com.bookmycut.entities.Notification;
import com.bookmycut.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserOrderByCreatedDateDesc(User user, Pageable pageable);
    
    List<Notification> findByUserAndReadFalseOrderByCreatedDateDesc(User user);
    
    long countByUserAndReadFalse(User user);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.read = false ORDER BY n.createdDate DESC")
    List<Notification> findUnreadNotifications(@Param("user") User user);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdDate >= :since ORDER BY n.createdDate DESC")
    List<Notification> findRecentNotifications(@Param("user") User user, @Param("since") LocalDateTime since);
    
    List<Notification> findByRelatedAppointmentIdAndType(Long appointmentId, Notification.NotificationType type);
}





