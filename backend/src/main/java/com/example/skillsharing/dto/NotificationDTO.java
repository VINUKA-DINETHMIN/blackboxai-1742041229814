package com.example.skillsharing.dto;

import com.example.skillsharing.model.Notification.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String message;
    private NotificationType type;
    private String actionUrl;
    private boolean read;
    private LocalDateTime createdAt;
    
    // Additional fields for context
    private UserDTO actor;        // User who triggered the notification
    private Long targetId;        // ID of the target resource (post, comment, etc.)
    private String targetType;    // Type of the target resource
    private String targetPreview; // Short preview/snippet of the target content
    
    // For batch operations
    public static class NotificationBatchDTO {
        private Long[] notificationIds;
        
        public Long[] getNotificationIds() {
            return notificationIds;
        }
        
        public void setNotificationIds(Long[] notificationIds) {
            this.notificationIds = notificationIds;
        }
    }
    
    // For creating notifications
    public static class CreateNotificationDTO {
        private Long recipientId;
        private NotificationType type;
        private String message;
        private String actionUrl;
        private Long actorId;
        private Long targetId;
        private String targetType;
        
        public Long getRecipientId() {
            return recipientId;
        }
        
        public void setRecipientId(Long recipientId) {
            this.recipientId = recipientId;
        }
        
        public NotificationType getType() {
            return type;
        }
        
        public void setType(NotificationType type) {
            this.type = type;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getActionUrl() {
            return actionUrl;
        }
        
        public void setActionUrl(String actionUrl) {
            this.actionUrl = actionUrl;
        }
        
        public Long getActorId() {
            return actorId;
        }
        
        public void setActorId(Long actorId) {
            this.actorId = actorId;
        }
        
        public Long getTargetId() {
            return targetId;
        }
        
        public void setTargetId(Long targetId) {
            this.targetId = targetId;
        }
        
        public String getTargetType() {
            return targetType;
        }
        
        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }
    }
}
