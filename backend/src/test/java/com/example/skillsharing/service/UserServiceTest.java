package com.example.skillsharing.service;

import com.example.skillsharing.dto.UserDTO;
import com.example.skillsharing.exception.ResourceNotFoundException;
import com.example.skillsharing.model.User;
import com.example.skillsharing.repository.UserRepository;
import com.example.skillsharing.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setBio("Test bio");
        testUser.setProfilePicture("profile.jpg");

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setBio("Test bio");
        testUserDTO.setProfilePicture("profile.jpg");
    }

    @Test
    void getUserByUsername_WhenUserExists_ReturnsUserDTO() {
        // Arrange
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.getUserByUsername(testUser.getUsername());

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findByUsername(testUser.getUsername());
    }

    @Test
    void getUserByUsername_WhenUserDoesNotExist_ThrowsException() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByUsername(username));
        verify(userRepository).findByUsername(username);
    }

    @Test
    void updateUser_WhenValidUpdate_ReturnsUpdatedUserDTO() {
        // Arrange
        Long userId = 1L;
        UserDTO updateDTO = new UserDTO();
        updateDTO.setBio("Updated bio");
        updateDTO.setProfilePicture("new-profile.jpg");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        // Act
        UserDTO result = userService.updateUser(userId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getBio(), result.getBio());
        assertEquals(updateDTO.getProfilePicture(), result.getProfilePicture());
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        UserDTO updateDTO = new UserDTO();
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(userId, updateDTO));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_WhenUserExists_DeletesUser() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_WhenUserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(userId));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void isFollowing_WhenUserIsFollowing_ReturnsTrue() {
        // Arrange
        Long userId = 1L;
        Long targetUserId = 2L;
        when(userRepository.existsByFollowerIdAndFollowingId(userId, targetUserId))
                .thenReturn(true);

        // Act
        boolean result = userService.isFollowing(userId, targetUserId);

        // Assert
        assertTrue(result);
        verify(userRepository).existsByFollowerIdAndFollowingId(userId, targetUserId);
    }

    @Test
    void isFollowing_WhenUserIsNotFollowing_ReturnsFalse() {
        // Arrange
        Long userId = 1L;
        Long targetUserId = 2L;
        when(userRepository.existsByFollowerIdAndFollowingId(userId, targetUserId))
                .thenReturn(false);

        // Act
        boolean result = userService.isFollowing(userId, targetUserId);

        // Assert
        assertFalse(result);
        verify(userRepository).existsByFollowerIdAndFollowingId(userId, targetUserId);
    }
}
