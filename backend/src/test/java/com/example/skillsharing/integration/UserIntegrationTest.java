package com.example.skillsharing.integration;

import com.example.skillsharing.dto.SignUpRequest;
import com.example.skillsharing.dto.UserDTO;
import com.example.skillsharing.model.User;
import com.example.skillsharing.repository.UserRepository;
import com.example.skillsharing.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        userRepository.deleteAll();

        // Create a test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setBio("Test bio");
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void registerUser_WithValidData_ReturnsCreatedUser() {
        // Arrange
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setBio("New user bio");

        // Act
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(
            "/api/auth/signup",
            signUpRequest,
            UserDTO.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(signUpRequest.getUsername(), response.getBody().getUsername());
        assertEquals(signUpRequest.getEmail(), response.getBody().getEmail());
        assertTrue(userRepository.existsByEmail(signUpRequest.getEmail()));
    }

    @Test
    @Transactional
    void getUserProfile_WithExistingUsername_ReturnsUserProfile() {
        // Act
        ResponseEntity<UserDTO> response = restTemplate.getForEntity(
            "/api/users/" + testUser.getUsername(),
            UserDTO.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUser.getUsername(), response.getBody().getUsername());
        assertEquals(testUser.getEmail(), response.getBody().getEmail());
    }

    @Test
    @Transactional
    void updateUserProfile_WithValidData_ReturnsUpdatedProfile() {
        // Arrange
        UserDTO updateRequest = new UserDTO();
        updateRequest.setBio("Updated bio");
        updateRequest.setProfilePicture("new-profile.jpg");

        // Act
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(
            "/api/users/me",
            updateRequest,
            UserDTO.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updateRequest.getBio(), response.getBody().getBio());
        assertEquals(updateRequest.getProfilePicture(), response.getBody().getProfilePicture());

        // Verify database update
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals(updateRequest.getBio(), updatedUser.getBio());
        assertEquals(updateRequest.getProfilePicture(), updatedUser.getProfilePicture());
    }

    @Test
    @Transactional
    void followUser_WhenUserExists_SuccessfullyFollows() {
        // Arrange
        User userToFollow = new User();
        userToFollow.setUsername("usertofollow");
        userToFollow.setEmail("follow@example.com");
        userToFollow.setPassword("password123");
        userToFollow = userRepository.save(userToFollow);

        // Act
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(
            "/api/users/" + userToFollow.getId() + "/follow",
            null,
            UserDTO.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(userService.isFollowing(testUser.getId(), userToFollow.getId()));
    }

    @Test
    @Transactional
    void unfollowUser_WhenFollowing_SuccessfullyUnfollows() {
        // Arrange
        User userToUnfollow = new User();
        userToUnfollow.setUsername("usertounfollow");
        userToUnfollow.setEmail("unfollow@example.com");
        userToUnfollow.setPassword("password123");
        userToUnfollow = userRepository.save(userToUnfollow);

        // First follow the user
        userService.followUser(testUser.getId(), userToUnfollow.getId());

        // Act
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(
            "/api/users/" + userToUnfollow.getId() + "/unfollow",
            null,
            UserDTO.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(userService.isFollowing(testUser.getId(), userToUnfollow.getId()));
    }

    @Test
    @Transactional
    void deleteUser_WhenUserExists_SuccessfullyDeletes() {
        // Act
        restTemplate.delete("/api/users/me");

        // Assert
        assertFalse(userRepository.existsById(testUser.getId()));
    }
}
