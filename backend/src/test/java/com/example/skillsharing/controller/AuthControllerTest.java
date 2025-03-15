package com.example.skillsharing.controller;

import com.example.skillsharing.base.BaseTest;
import com.example.skillsharing.dto.AuthResponse;
import com.example.skillsharing.dto.LoginRequest;
import com.example.skillsharing.dto.SignUpRequest;
import com.example.skillsharing.model.User;
import com.example.skillsharing.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends BaseTest {

    @Test
    void signup_WithValidData_ReturnsCreatedUser() throws Exception {
        // Arrange
        SignUpRequest signUpRequest = TestDataFactory.createSignUpRequest();

        // Act
        ResultActions result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(signUpRequest)));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")));

        // Verify user was created in database
        assertTrue(userRepository.existsByEmail(signUpRequest.getEmail()));
        assertTrue(userRepository.existsByUsername(signUpRequest.getUsername()));
    }

    @Test
    void signup_WithExistingEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        User existingUser = TestDataFactory.createUser(null);
        userRepository.save(existingUser);

        SignUpRequest signUpRequest = TestDataFactory.createSignUpRequest();
        signUpRequest.setEmail(existingUser.getEmail());

        // Act
        ResultActions result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(signUpRequest)));

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Email address already in use")));
    }

    @Test
    void signup_WithExistingUsername_ReturnsBadRequest() throws Exception {
        // Arrange
        User existingUser = TestDataFactory.createUser(null);
        userRepository.save(existingUser);

        SignUpRequest signUpRequest = TestDataFactory.createSignUpRequest();
        signUpRequest.setUsername(existingUser.getUsername());

        // Act
        ResultActions result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(signUpRequest)));

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Username already taken")));
    }

    @Test
    void login_WithValidCredentials_ReturnsAuthToken() throws Exception {
        // Arrange
        LoginRequest loginRequest = TestDataFactory.createLoginRequest();
        loginRequest.setEmail(testUser.getEmail());
        loginRequest.setPassword("password123");

        // Act
        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")));

        String responseString = result.andReturn().getResponse().getContentAsString();
        AuthResponse response = objectMapper.readValue(responseString, AuthResponse.class);
        assertNotNull(response.getAccessToken());
    }

    @Test
    void login_WithInvalidEmail_ReturnsUnauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = TestDataFactory.createLoginRequest();
        loginRequest.setEmail("nonexistent@example.com");

        // Act
        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)));

        // Assert
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithInvalidPassword_ReturnsUnauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = TestDataFactory.createLoginRequest();
        loginRequest.setEmail(testUser.getEmail());
        loginRequest.setPassword("wrongpassword");

        // Act
        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)));

        // Assert
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void signup_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        SignUpRequest signUpRequest = TestDataFactory.createSignUpRequest();
        signUpRequest.setEmail("invalid-email");

        // Act
        ResultActions result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(signUpRequest)));

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Email should be valid")));
    }

    @Test
    void signup_WithShortPassword_ReturnsBadRequest() throws Exception {
        // Arrange
        SignUpRequest signUpRequest = TestDataFactory.createSignUpRequest();
        signUpRequest.setPassword("short");

        // Act
        ResultActions result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(signUpRequest)));

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Password must be at least 6 characters")));
    }

    @Test
    void signup_WithEmptyUsername_ReturnsBadRequest() throws Exception {
        // Arrange
        SignUpRequest signUpRequest = TestDataFactory.createSignUpRequest();
        signUpRequest.setUsername("");

        // Act
        ResultActions result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(signUpRequest)));

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Username is required")));
    }
}
