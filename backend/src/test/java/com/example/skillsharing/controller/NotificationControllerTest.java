package com.example.skillsharing.controller;

import com.example.skillsharing.base.BaseTest;
import com.example.skillsharing.model.Notification;
import com.example.skillsharing.model.Notification.NotificationType;
import com.example.skillsharing.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationControllerTest extends BaseTest {

    @Test
    void getUserNotifications_ReturnsPaginatedNotifications() throws Exception {
        // Arrange
        List<Notification> notifications = TestDataFactory.createNotifications(3, testUser);
        notificationRepository.saveAll(notifications);

        // Act
        ResultActions result = mockMvc.perform(get("/api/notifications")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void getNotification_WithValidId_ReturnsNotification() throws Exception {
        // Arrange
        Notification notification = TestDataFactory.createNotification(null, testUser);
        notification = notificationRepository.save(notification);

        // Act
        ResultActions result = mockMvc.perform(get("/api/notifications/{notificationId}", notification.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notification.getId().intValue())))
                .andExpect(jsonPath("$.title", is(notification.getTitle())))
                .andExpect(jsonPath("$.message", is(notification.getMessage())));
    }

    @Test
    void markNotificationAsRead_UpdatesReadStatus() throws Exception {
        // Arrange
        Notification notification = TestDataFactory.createNotification(null, testUser);
        notification.setRead(false);
        notification = notificationRepository.save(notification);

        // Act
        ResultActions result = mockMvc.perform(post("/api/notifications/{notificationId}/mark-read", notification.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        assertTrue(notificationRepository.findById(notification.getId()).get().isRead());
    }

    @Test
    void markAllNotificationsAsRead_UpdatesAllReadStatus() throws Exception {
        // Arrange
        List<Notification> notifications = TestDataFactory.createNotifications(3, testUser);
        notifications.forEach(n -> n.setRead(false));
        notificationRepository.saveAll(notifications);

        // Act
        ResultActions result = mockMvc.perform(post("/api/notifications/mark-all-read")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        List<Notification> updatedNotifications = notificationRepository.findByUserId(testUser.getId());
        assertTrue(updatedNotifications.stream().allMatch(Notification::isRead));
    }

    @Test
    void deleteNotification_WithValidId_DeletesNotification() throws Exception {
        // Arrange
        Notification notification = TestDataFactory.createNotification(null, testUser);
        notification = notificationRepository.save(notification);

        // Act
        ResultActions result = mockMvc.perform(delete("/api/notifications/{notificationId}", notification.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        assertFalse(notificationRepository.existsById(notification.getId()));
    }

    @Test
    void deleteAllNotifications_DeletesAllUserNotifications() throws Exception {
        // Arrange
        List<Notification> notifications = TestDataFactory.createNotifications(3, testUser);
        notificationRepository.saveAll(notifications);

        // Act
        ResultActions result = mockMvc.perform(delete("/api/notifications")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        assertTrue(notificationRepository.findByUserId(testUser.getId()).isEmpty());
    }

    @Test
    void getUnreadCount_ReturnsCorrectCount() throws Exception {
        // Arrange
        List<Notification> notifications = TestDataFactory.createNotifications(3, testUser);
        notifications.forEach(n -> n.setRead(false));
        notificationRepository.saveAll(notifications);

        // Act
        ResultActions result = mockMvc.perform(get("/api/notifications/unread-count")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void hasUnreadNotifications_WithUnreadNotifications_ReturnsTrue() throws Exception {
        // Arrange
        Notification notification = TestDataFactory.createNotification(null, testUser);
        notification.setRead(false);
        notificationRepository.save(notification);

        // Act
        ResultActions result = mockMvc.perform(get("/api/notifications/has-unread")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void cleanupOldNotifications_DeletesOldNotifications() throws Exception {
        // Arrange
        List<Notification> notifications = TestDataFactory.createNotifications(3, testUser);
        notifications.forEach(n -> n.setCreatedAt(n.getCreatedAt().minusDays(31))); // Older than 30 days
        notificationRepository.saveAll(notifications);

        // Act
        ResultActions result = mockMvc.perform(post("/api/notifications/cleanup")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        assertTrue(notificationRepository.findByUserId(testUser.getId()).isEmpty());
    }

    @Test
    void getNotification_WithInvalidId_ReturnsNotFound() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/api/notifications/{notificationId}", 999L)
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isNotFound());
    }

    @Test
    void getNotification_WithUnauthorizedUser_ReturnsForbidden() throws Exception {
        // Arrange
        User otherUser = TestDataFactory.createUser(null);
        otherUser = userRepository.save(otherUser);
        
        Notification notification = TestDataFactory.createNotification(null, otherUser);
        notification = notificationRepository.save(notification);

        // Act
        ResultActions result = mockMvc.perform(get("/api/notifications/{notificationId}", notification.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isForbidden());
    }
}
