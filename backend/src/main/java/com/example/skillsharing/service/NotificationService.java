package com.example.skillsharing.service;

import com.example.skillsharing.dto.NotificationDTO;
import com.example.skillsharing.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    
    NotificationDTO createNotification(NotificationDTO.CreateNotificationDTO createNotificationDTO);
    
    NotificationDTO getNotificationById(Long notificationId);
    
    Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable);
    
    void markAsRead(Long notificationId);
    
    void markAllAsRead(Long userId);
    
    void deleteNotification(Long notificationId);
    
    void deleteAllNotifications(Long userId);
    
    long getUnreadCount(Long userId);
    
    boolean hasUnreadNotifications(Long userId);
    
    void cleanupOldNotifications(Long userId);
    
    NotificationDTO convertToDTO(Notification notification);
    
    Notification convertToEntity(NotificationDTO notificationDTO);
    
    // Helper methods for creating specific types of notifications
    void createLikeNotification(Long actorId, Long postId);
    
    void createCommentNotification(Long actorId, Long postId, Long commentId);
    
    void createFollowNotification(Long actorId, Long targetUserId);
}
