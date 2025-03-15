package com.example.skillsharing.controller;

import com.example.skillsharing.dto.NotificationDTO;
import com.example.skillsharing.security.CurrentUser;
import com.example.skillsharing.security.UserPrincipal;
import com.example.skillsharing.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public Page<NotificationDTO> getUserNotifications(@CurrentUser UserPrincipal currentUser,
                                                    Pageable pageable) {
        return notificationService.getUserNotifications(currentUser.getId(), pageable);
    }

    @GetMapping("/{notificationId}")
    @PreAuthorize("hasRole('USER')")
    public NotificationDTO getNotification(@PathVariable Long notificationId) {
        return notificationService.getNotificationById(notificationId);
    }

    @PostMapping("/{notificationId}/mark-read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> markAllNotificationsAsRead(@CurrentUser UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteAllNotifications(@CurrentUser UserPrincipal currentUser) {
        notificationService.deleteAllNotifications(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Long> getUnreadCount(@CurrentUser UserPrincipal currentUser) {
        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/has-unread")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> hasUnreadNotifications(@CurrentUser UserPrincipal currentUser) {
        boolean hasUnread = notificationService.hasUnreadNotifications(currentUser.getId());
        return ResponseEntity.ok(hasUnread);
    }

    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> cleanupOldNotifications(@CurrentUser UserPrincipal currentUser) {
        notificationService.cleanupOldNotifications(currentUser.getId());
        return ResponseEntity.ok().build();
    }
}
