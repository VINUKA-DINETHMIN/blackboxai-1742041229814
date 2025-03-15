package com.example.skillsharing.security;

import com.example.skillsharing.exception.ResourceNotFoundException;
import com.example.skillsharing.model.User;
import com.example.skillsharing.repository.UserRepository;
import com.example.skillsharing.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createUser(1L);
    }

    @Test
    void loadUserByUsername_WithValidEmail_ReturnsUserDetails() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getEmail());

        // Assert
        assertNotNull(userDetails);
        assertEquals(testUser.getEmail(), userDetails.getUsername());
        assertTrue(userDetails instanceof UserPrincipal);
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        assertEquals(testUser.getId(), userPrincipal.getId());
    }

    @Test
    void loadUserByUsername_WithInvalidEmail_ThrowsException() {
        // Arrange
        String invalidEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(invalidEmail))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(invalidEmail));
    }

    @Test
    void loadUserById_WithValidId_ReturnsUserDetails() {
        // Arrange
        when(userRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserById(testUser.getId());

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof UserPrincipal);
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        assertEquals(testUser.getId(), userPrincipal.getId());
        assertEquals(testUser.getEmail(), userPrincipal.getEmail());
    }

    @Test
    void loadUserById_WithInvalidId_ThrowsException() {
        // Arrange
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userDetailsService.loadUserById(999L));
    }

    @Test
    void loadUserByUsername_WithNullEmail_ThrowsException() {
        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(null));
    }

    @Test
    void loadUserByUsername_WithEmptyEmail_ThrowsException() {
        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(""));
    }

    @Test
    void loadUserById_WithNullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                userDetailsService.loadUserById(null));
    }

    @Test
    void loadUserByUsername_PreservesUserAuthorities() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getEmail());

        // Assert
        assertNotNull(userDetails.getAuthorities());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserById_HandlesRepositoryException() {
        // Arrange
        when(userRepository.findById(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                userDetailsService.loadUserById(1L));
    }

    @Test
    void loadUserByUsername_HandlesRepositoryException() {
        // Arrange
        when(userRepository.findByEmail(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                userDetailsService.loadUserByUsername("test@example.com"));
    }

    @Test
    void userPrincipal_EqualsAndHashCode() {
        // Arrange
        UserPrincipal principal1 = UserPrincipal.create(testUser);
        UserPrincipal principal2 = UserPrincipal.create(testUser);
        UserPrincipal differentPrincipal = UserPrincipal.create(
                TestDataFactory.createUser(2L));

        // Assert
        assertEquals(principal1, principal2);
        assertEquals(principal1.hashCode(), principal2.hashCode());
        assertNotEquals(principal1, differentPrincipal);
        assertNotEquals(principal1.hashCode(), differentPrincipal.hashCode());
    }
}
