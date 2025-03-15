package com.example.skillsharing.repository;

import com.example.skillsharing.model.Notification;
import com.example.skillsharing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);
    
    long countByRecipientAndReadFalse(User recipient);
    
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient = :recipient AND n.read = false")
    void markAllAsRead(User recipient);
    
    void deleteByRecipientAndCreatedAtBefore(User recipient, java.time.LocalDateTime date);
    
    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.recipient = :recipient AND n.read = false")
    boolean hasUnreadNotifications(User recipient);
}
