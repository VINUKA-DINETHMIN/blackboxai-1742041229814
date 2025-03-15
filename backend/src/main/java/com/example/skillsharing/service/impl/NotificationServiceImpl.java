package com.example.skillsharing.service.impl;

import com.example.skillsharing.dto.NotificationDTO;
import com.example.skillsharing.exception.ResourceNotFoundException;
import com.example.skillsharing.model.Comment;
import com.example.skillsharing.model.Notification;
import com.example.skillsharing.model.SkillSharingPost;
import com.example.skillsharing.model.User;
import com.example.skillsharing.repository.CommentRepository;
import com.example.skillsharing.repository.NotificationRepository;
import com.example.skillsharing.repository.SkillSharingPostRepository;
import com.example.skillsharing.service.NotificationService;
import com.example.skillsharing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final SkillSharingPostRepository postRepository;
    private final CommentRepository commentRepository;
    private static final int NOTIFICATION_RETENTION_DAYS = 30;

    @Override
    public NotificationDTO createNotification(NotificationDTO.CreateNotificationDTO createDTO) {
        User recipient = userService.getUserEntityById(createDTO.getRecipientId());
        User actor = userService.getUserEntityById(createDTO.getActorId());

        // Don't create notification if user is acting on their own content
        if (recipient.getId().equals(actor.getId())) {
            return null;
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(createDTO.getType());
        notification.setMessage(createDTO.getMessage());
        notification.setActionUrl(createDTO.getActionUrl());
        notification.setRead(false);

        Notification savedNotification = notificationRepository.save(notification);
        return convertToDTO(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Long notificationId) {
        Notification notification = getNotificationEntityById(notificationId);
        return convertToDTO(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
        User user = userService.getUserEntityById(userId);
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = getNotificationEntityById(notificationId);
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        User user = userService.getUserEntityById(userId);
        notificationRepository.markAllAsRead(user);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public void deleteAllNotifications(Long userId) {
        User user = userService.getUserEntityById(userId);
        notificationRepository.deleteByRecipientAndCreatedAtBefore(user, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        User user = userService.getUserEntityById(userId);
        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUnreadNotifications(Long userId) {
        User user = userService.getUserEntityById(userId);
        return notificationRepository.hasUnreadNotifications(user);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    public void cleanupOldNotifications(Long userId) {
        User user = userService.getUserEntityById(userId);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(NOTIFICATION_RETENTION_DAYS);
        notificationRepository.deleteByRecipientAndCreatedAtBefore(user, cutoffDate);
    }

    @Override
    public void createLikeNotification(Long actorId, Long postId) {
        User actor = userService.getUserEntityById(actorId);
        SkillSharingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        NotificationDTO.CreateNotificationDTO createDTO = new NotificationDTO.CreateNotificationDTO();
        createDTO.setRecipientId(post.getUser().getId());
        createDTO.setActorId(actorId);
        createDTO.setType(Notification.NotificationType.LIKE);
        createDTO.setMessage(actor.getUsername() + " liked your post");
        createDTO.setActionUrl("/posts/" + postId);
        createDTO.setTargetId(postId);
        createDTO.setTargetType("post");
        
        createNotification(createDTO);
    }

    @Override
    public void createCommentNotification(Long actorId, Long postId, Long commentId) {
        User actor = userService.getUserEntityById(actorId);
        SkillSharingPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        NotificationDTO.CreateNotificationDTO createDTO = new NotificationDTO.CreateNotificationDTO();
        createDTO.setRecipientId(post.getUser().getId());
        createDTO.setActorId(actorId);
        createDTO.setType(Notification.NotificationType.COMMENT);
        createDTO.setMessage(actor.getUsername() + " commented on your post: " + 
                           comment.getContent().substring(0, Math.min(50, comment.getContent().length())));
        createDTO.setActionUrl("/posts/" + postId + "#comment-" + commentId);
        createDTO.setTargetId(commentId);
        createDTO.setTargetType("comment");
        
        createNotification(createDTO);
    }

    @Override
    public void createFollowNotification(Long actorId, Long targetUserId) {
        User actor = userService.getUserEntityById(actorId);
        
        NotificationDTO.CreateNotificationDTO createDTO = new NotificationDTO.CreateNotificationDTO();
        createDTO.setRecipientId(targetUserId);
        createDTO.setActorId(actorId);
        createDTO.setType(Notification.NotificationType.FOLLOW);
        createDTO.setMessage(actor.getUsername() + " started following you");
        createDTO.setActionUrl("/users/" + actor.getUsername());
        createDTO.setTargetId(actorId);
        createDTO.setTargetType("user");
        
        createNotification(createDTO);
    }

    @Override
    public NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setActionUrl(notification.getActionUrl());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    @Override
    public Notification convertToEntity(NotificationDTO notificationDTO) {
        Notification notification = new Notification();
        notification.setMessage(notificationDTO.getMessage());
        notification.setType(notificationDTO.getType());
        notification.setActionUrl(notificationDTO.getActionUrl());
        notification.setRead(notificationDTO.isRead());
        return notification;
    }

    private Notification getNotificationEntityById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
    }
}
