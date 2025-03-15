package com.example.skillsharing.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookieUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private static final String COOKIE_NAME = "test_cookie";
    private static final String COOKIE_VALUE = "test_value";

    @Test
    void getCookie_WithExistingCookie_ReturnsCookie() {
        // Arrange
        Cookie expectedCookie = new Cookie(COOKIE_NAME, COOKIE_VALUE);
        when(request.getCookies()).thenReturn(new Cookie[]{expectedCookie});

        // Act
        Optional<Cookie> result = CookieUtils.getCookie(request, COOKIE_NAME);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(COOKIE_NAME, result.get().getName());
        assertEquals(COOKIE_VALUE, result.get().getValue());
    }

    @Test
    void getCookie_WithNoCookies_ReturnsEmpty() {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        Optional<Cookie> result = CookieUtils.getCookie(request, COOKIE_NAME);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getCookie_WithDifferentCookies_ReturnsEmpty() {
        // Arrange
        Cookie otherCookie = new Cookie("other_cookie", "other_value");
        when(request.getCookies()).thenReturn(new Cookie[]{otherCookie});

        // Act
        Optional<Cookie> result = CookieUtils.getCookie(request, COOKIE_NAME);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void addCookie_CreatesCookieWithCorrectAttributes() {
        // Act
        CookieUtils.addCookie(response, COOKIE_NAME, COOKIE_VALUE, 3600);

        // Assert
        verify(response).addCookie(argThat(cookie -> 
            cookie.getName().equals(COOKIE_NAME) &&
            cookie.getValue().equals(COOKIE_VALUE) &&
            cookie.getMaxAge() == 3600 &&
            cookie.getPath().equals("/") &&
            cookie.isHttpOnly()
        ));
    }

    @Test
    void deleteCookie_WithExistingCookie_DeletesCookie() {
        // Arrange
        Cookie existingCookie = new Cookie(COOKIE_NAME, COOKIE_VALUE);
        when(request.getCookies()).thenReturn(new Cookie[]{existingCookie});
        when(request.getServerName()).thenReturn("localhost");

        // Act
        CookieUtils.deleteCookie(request, response, COOKIE_NAME);

        // Assert
        verify(response).addCookie(argThat(cookie -> 
            cookie.getName().equals(COOKIE_NAME) &&
            cookie.getValue().isEmpty() &&
            cookie.getPath().equals("/") &&
            cookie.getMaxAge() == 0
        ));
    }

    @Test
    void deleteCookie_WithNoCookies_DoesNothing() {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        CookieUtils.deleteCookie(request, response, COOKIE_NAME);

        // Assert
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void serialize_WithValidObject_ReturnsSerializedString() {
        // Arrange
        TestObject testObject = new TestObject("test");

        // Act
        String serialized = CookieUtils.serialize(testObject);

        // Assert
        assertNotNull(serialized);
        assertTrue(serialized.length() > 0);
    }

    @Test
    void deserialize_WithValidString_ReturnsDeserializedObject() {
        // Arrange
        TestObject original = new TestObject("test");
        String serialized = CookieUtils.serialize(original);

        // Act
        TestObject deserialized = CookieUtils.deserialize(serialized, TestObject.class);

        // Assert
        assertNotNull(deserialized);
        assertEquals(original.getValue(), deserialized.getValue());
    }

    @Test
    void deserialize_WithInvalidString_ReturnsNull() {
        // Act
        TestObject result = CookieUtils.deserialize("invalid_serialized_string", TestObject.class);

        // Assert
        assertNull(result);
    }

    @Test
    void deserialize_WithNullInput_ReturnsNull() {
        // Act
        TestObject result = CookieUtils.deserialize(null, TestObject.class);

        // Assert
        assertNull(result);
    }

    @Test
    void addCookie_WithZeroMaxAge_CreatesCookieWithCorrectAttributes() {
        // Act
        CookieUtils.addCookie(response, COOKIE_NAME, COOKIE_VALUE, 0);

        // Assert
        verify(response).addCookie(argThat(cookie -> 
            cookie.getName().equals(COOKIE_NAME) &&
            cookie.getValue().equals(COOKIE_VALUE) &&
            cookie.getMaxAge() == 0 &&
            cookie.getPath().equals("/") &&
            cookie.isHttpOnly()
        ));
    }

    @Test
    void addCookie_WithNegativeMaxAge_CreatesCookieWithCorrectAttributes() {
        // Act
        CookieUtils.addCookie(response, COOKIE_NAME, COOKIE_VALUE, -1);

        // Assert
        verify(response).addCookie(argThat(cookie -> 
            cookie.getName().equals(COOKIE_NAME) &&
            cookie.getValue().equals(COOKIE_VALUE) &&
            cookie.getMaxAge() == -1 &&
            cookie.getPath().equals("/") &&
            cookie.isHttpOnly()
        ));
    }

    // Helper class for serialization tests
    private static class TestObject {
        private String value;

        public TestObject() {
        }

        public TestObject(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
