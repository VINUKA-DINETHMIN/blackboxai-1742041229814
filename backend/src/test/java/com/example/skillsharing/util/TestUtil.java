package com.example.skillsharing.util;

import com.example.skillsharing.dto.LoginRequest;
import com.example.skillsharing.dto.SignUpRequest;
import com.example.skillsharing.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

public class TestUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setAuthentication(User user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            user,
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public static SignUpRequest createSignUpRequest() {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setBio("Test bio");
        return request;
    }

    public static LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        return request;
    }

    public static User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setBio("Test bio");
        return user;
    }

    public static MockMultipartFile createMockImageFile() {
        return new MockMultipartFile(
            "file",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );
    }

    public static ResultActions performPost(MockMvc mockMvc, String url, Object content) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
            .post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(asJsonString(content));

        return mockMvc.perform(builder);
    }

    public static ResultActions performGet(MockMvc mockMvc, String url) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
            .get(url)
            .accept(MediaType.APPLICATION_JSON);

        return mockMvc.perform(builder);
    }

    public static ResultActions performPut(MockMvc mockMvc, String url, Object content) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
            .put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(asJsonString(content));

        return mockMvc.perform(builder);
    }

    public static ResultActions performDelete(MockMvc mockMvc, String url) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
            .delete(url);

        return mockMvc.perform(builder);
    }

    public static ResultActions performFileUpload(MockMvc mockMvc, String url, MockMultipartFile file) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
            .multipart(url)
            .file(file);

        return mockMvc.perform(builder);
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    public static String createTestToken() {
        return "test-jwt-token";
    }

    public static String createAuthorizationHeader() {
        return "Bearer " + createTestToken();
    }
}
