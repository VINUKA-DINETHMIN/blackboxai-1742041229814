package com.example.skillsharing.security.oauth2;

import com.example.skillsharing.exception.OAuth2AuthenticationProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OAuth2UserInfoFactoryTest {

    @Test
    void getOAuth2UserInfo_WithGoogleProvider_ReturnsGoogleOAuth2UserInfo() {
        // Arrange
        String registrationId = "google";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("name", "Test User");
        attributes.put("email", "test@example.com");
        attributes.put("picture", "https://example.com/picture.jpg");

        // Act
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        // Assert
        assertTrue(userInfo instanceof GoogleOAuth2UserInfo);
        assertEquals("123456789", userInfo.getId());
        assertEquals("Test User", userInfo.getName());
        assertEquals("test@example.com", userInfo.getEmail());
        assertEquals("https://example.com/picture.jpg", userInfo.getImageUrl());
    }

    @Test
    void getOAuth2UserInfo_WithInvalidProvider_ThrowsException() {
        // Arrange
        String registrationId = "invalid_provider";
        Map<String, Object> attributes = new HashMap<>();

        // Act & Assert
        OAuth2AuthenticationProcessingException exception = assertThrows(
            OAuth2AuthenticationProcessingException.class,
            () -> OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes)
        );
        assertEquals("Sorry! Login with invalid_provider is not supported yet.", exception.getMessage());
    }

    @Test
    void getOAuth2UserInfo_WithNullAttributes_ThrowsException() {
        // Arrange
        String registrationId = "google";

        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, null)
        );
    }

    @Test
    void getOAuth2UserInfo_WithEmptyAttributes_ReturnsUserInfoWithNullValues() {
        // Arrange
        String registrationId = "google";
        Map<String, Object> attributes = new HashMap<>();

        // Act
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        // Assert
        assertNotNull(userInfo);
        assertNull(userInfo.getId());
        assertNull(userInfo.getName());
        assertNull(userInfo.getEmail());
        assertNull(userInfo.getImageUrl());
    }

    @Test
    void googleOAuth2UserInfo_WithAllAttributes_ReturnsCorrectValues() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("name", "Test User");
        attributes.put("email", "test@example.com");
        attributes.put("picture", "https://example.com/picture.jpg");

        // Act
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // Assert
        assertEquals("123456789", userInfo.getId());
        assertEquals("Test User", userInfo.getName());
        assertEquals("test@example.com", userInfo.getEmail());
        assertEquals("https://example.com/picture.jpg", userInfo.getImageUrl());
    }

    @Test
    void googleOAuth2UserInfo_WithMissingAttributes_ReturnsNullValues() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();

        // Act
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // Assert
        assertNull(userInfo.getId());
        assertNull(userInfo.getName());
        assertNull(userInfo.getEmail());
        assertNull(userInfo.getImageUrl());
    }

    @Test
    void googleOAuth2UserInfo_WithNonStringAttributes_HandlesTypesGracefully() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", 123456789);
        attributes.put("name", new StringBuilder("Test User"));
        attributes.put("email", new Object());
        attributes.put("picture", null);

        // Act
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // Assert
        assertEquals("123456789", userInfo.getId());
        assertEquals("Test User", userInfo.getName());
        assertEquals(userInfo.getEmail(), userInfo.getEmail());
        assertNull(userInfo.getImageUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void googleOAuth2UserInfo_WithEmptyStrings_ReturnsEmptyValues(String emptyValue) {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", emptyValue);
        attributes.put("name", emptyValue);
        attributes.put("email", emptyValue);
        attributes.put("picture", emptyValue);

        // Act
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // Assert
        assertEquals(emptyValue, userInfo.getId());
        assertEquals(emptyValue, userInfo.getName());
        assertEquals(emptyValue, userInfo.getEmail());
        assertEquals(emptyValue, userInfo.getImageUrl());
    }

    @Test
    void googleOAuth2UserInfo_GetAttributes_ReturnsUnmodifiableMap() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // Act & Assert
        assertThrows(
            UnsupportedOperationException.class,
            () -> userInfo.getAttributes().put("newKey", "newValue")
        );
    }
}
