package com.example.skillsharing.controller;

import com.example.skillsharing.base.BaseTest;
import com.example.skillsharing.dto.UserDTO;
import com.example.skillsharing.model.User;
import com.example.skillsharing.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest extends BaseTest {

    @Test
    void getCurrentUser_ReturnsAuthenticatedUser() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/api/users/me")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is(testUser.getUsername())))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())));
    }

    @Test
    void getCurrentUser_WithoutAuth_ReturnsUnauthorized() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/api/users/me"));

        // Assert
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void getUserProfile_WithValidUsername_ReturnsUserProfile() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/api/users/{username}", testUser.getUsername())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(testUser.getUsername())))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())));
    }

    @Test
    void getUserProfile_WithInvalidUsername_ReturnsNotFound() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/api/users/nonexistent")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isNotFound());
    }

    @Test
    void updateUser_WithValidData_ReturnsUpdatedProfile() throws Exception {
        // Arrange
        UserDTO updateRequest = new UserDTO();
        updateRequest.setBio("Updated bio");
        updateRequest.setProfilePicture("new-profile.jpg");

        // Act
        ResultActions result = mockMvc.perform(put("/api/users/me")
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateRequest)));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.bio", is(updateRequest.getBio())))
                .andExpect(jsonPath("$.profilePicture", is(updateRequest.getProfilePicture())));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals(updateRequest.getBio(), updatedUser.getBio());
        assertEquals(updateRequest.getProfilePicture(), updatedUser.getProfilePicture());
    }

    @Test
    void updateProfilePicture_WithValidFile_ReturnsUpdatedProfile() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        // Act
        ResultActions result = mockMvc.perform(multipart("/api/users/me/profile-picture")
                .file(file)
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePicture", notNullValue()));
    }

    @Test
    void followUser_WhenUserExists_SuccessfullyFollows() throws Exception {
        // Arrange
        User userToFollow = TestDataFactory.createUser(null);
        userToFollow = userRepository.save(userToFollow);

        // Act
        ResultActions result = mockMvc.perform(post("/api/users/{userId}/follow", userToFollow.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        assertTrue(userRepository.existsByFollowerIdAndFollowingId(testUser.getId(), userToFollow.getId()));
    }

    @Test
    void unfollowUser_WhenFollowing_SuccessfullyUnfollows() throws Exception {
        // Arrange
        User userToUnfollow = TestDataFactory.createUser(null);
        userToUnfollow = userRepository.save(userToUnfollow);
        testUser.getFollowing().add(userToUnfollow);
        userRepository.save(testUser);

        // Act
        ResultActions result = mockMvc.perform(post("/api/users/{userId}/unfollow", userToUnfollow.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        assertFalse(userRepository.existsByFollowerIdAndFollowingId(testUser.getId(), userToUnfollow.getId()));
    }

    @Test
    void getUserFollowers_ReturnsFollowersList() throws Exception {
        // Arrange
        List<User> followers = TestDataFactory.createUsers(3);
        followers.forEach(follower -> {
            follower = userRepository.save(follower);
            follower.getFollowing().add(testUser);
            userRepository.save(follower);
        });

        // Act
        ResultActions result = mockMvc.perform(get("/api/users/{userId}/followers", testUser.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void getUserFollowing_ReturnsFollowingList() throws Exception {
        // Arrange
        List<User> following = TestDataFactory.createUsers(3);
        following.forEach(followedUser -> {
            followedUser = userRepository.save(followedUser);
            testUser.getFollowing().add(followedUser);
        });
        userRepository.save(testUser);

        // Act
        ResultActions result = mockMvc.perform(get("/api/users/{userId}/following", testUser.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void deleteUser_SuccessfullyDeletesUser() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(delete("/api/users/me")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        assertFalse(userRepository.existsById(testUser.getId()));
    }

    @Test
    void isFollowing_WhenFollowing_ReturnsTrue() throws Exception {
        // Arrange
        User userToCheck = TestDataFactory.createUser(null);
        userToCheck = userRepository.save(userToCheck);
        testUser.getFollowing().add(userToCheck);
        userRepository.save(testUser);

        // Act
        ResultActions result = mockMvc.perform(get("/api/users/{userId}/is-following", userToCheck.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
