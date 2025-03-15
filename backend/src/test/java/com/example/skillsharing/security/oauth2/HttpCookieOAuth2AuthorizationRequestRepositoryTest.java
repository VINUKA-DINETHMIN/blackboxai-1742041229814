package com.example.skillsharing.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    private HttpCookieOAuth2AuthorizationRequestRepository repository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private OAuth2AuthorizationRequest authorizationRequest;

    @BeforeEach
    void setUp() {
        repository = new HttpCookieOAuth2AuthorizationRequestRepository();
        authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://example.com/oauth2/authorize")
                .clientId("client-id")
                .redirectUri("http://localhost:8080/oauth2/callback")
                .state("state")
                .build();
    }

    @Test
    void loadAuthorizationRequest_WithValidCookie_ReturnsRequest() {
        // Arrange
        String serializedRequest = CookieUtils.serialize(authorizationRequest);
        Cookie cookie = new Cookie(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                serializedRequest);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        // Act
        OAuth2AuthorizationRequest loadedRequest = repository.loadAuthorizationRequest(request);

        // Assert
        assertNotNull(loadedRequest);
        assertEquals(authorizationRequest.getClientId(), loadedRequest.getClientId());
        assertEquals(authorizationRequest.getAuthorizationUri(), loadedRequest.getAuthorizationUri());
        assertEquals(authorizationRequest.getRedirectUri(), loadedRequest.getRedirectUri());
    }

    @Test
    void loadAuthorizationRequest_WithNoCookie_ReturnsNull() {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        OAuth2AuthorizationRequest loadedRequest = repository.loadAuthorizationRequest(request);

        // Assert
        assertNull(loadedRequest);
    }

    @Test
    void saveAuthorizationRequest_WithValidRequest_SavesCookie() {
        // Act
        repository.saveAuthorizationRequest(authorizationRequest, request, response);

        // Assert
        verify(response, times(2)).addCookie(any(Cookie.class));
    }

    @Test
    void saveAuthorizationRequest_WithNullRequest_RemovesCookie() {
        // Act
        repository.saveAuthorizationRequest(null, request, response);

        // Assert
        verify(response, times(2)).addCookie(argThat(cookie -> 
            cookie.getMaxAge() == 0
        ));
    }

    @Test
    void removeAuthorizationRequest_ReturnsAndRemovesRequest() {
        // Arrange
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        String serializedRequest = CookieUtils.serialize(authorizationRequest);
        mockRequest.setCookies(new Cookie(
            HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
            serializedRequest
        ));

        // Act
        OAuth2AuthorizationRequest removedRequest = repository.removeAuthorizationRequest(mockRequest, mockResponse);

        // Assert
        assertNotNull(removedRequest);
        assertEquals(authorizationRequest.getClientId(), removedRequest.getClientId());
        assertTrue(mockResponse.getCookies().length > 0);
        assertEquals(0, mockResponse.getCookies()[0].getMaxAge());
    }

    @Test
    void removeAuthorizationRequestCookies_RemovesBothCookies() {
        // Act
        repository.removeAuthorizationRequestCookies(request, response);

        // Assert
        verify(response, times(2)).addCookie(argThat(cookie -> 
            cookie.getMaxAge() == 0
        ));
    }

    @Test
    void loadAuthorizationRequest_WithInvalidCookie_ReturnsNull() {
        // Arrange
        Cookie cookie = new Cookie(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                "invalid_serialized_data");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        // Act
        OAuth2AuthorizationRequest loadedRequest = repository.loadAuthorizationRequest(request);

        // Assert
        assertNull(loadedRequest);
    }

    @Test
    void saveAuthorizationRequest_WithRedirectUri_SavesRedirectUriCookie() {
        // Arrange
        String redirectUri = "http://localhost:3000/oauth2/redirect";
        when(request.getParameter(eq("redirect_uri"))).thenReturn(redirectUri);

        // Act
        repository.saveAuthorizationRequest(authorizationRequest, request, response);

        // Assert
        verify(response).addCookie(argThat(cookie -> 
            cookie.getName().equals(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME) &&
            cookie.getValue().equals(redirectUri)
        ));
    }

    @Test
    void loadAuthorizationRequest_WithMultipleCookies_FindsCorrectCookie() {
        // Arrange
        String serializedRequest = CookieUtils.serialize(authorizationRequest);
        Cookie authCookie = new Cookie(
            HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
            serializedRequest
        );
        Cookie otherCookie = new Cookie("other_cookie", "other_value");
        when(request.getCookies()).thenReturn(new Cookie[]{otherCookie, authCookie});

        // Act
        OAuth2AuthorizationRequest loadedRequest = repository.loadAuthorizationRequest(request);

        // Assert
        assertNotNull(loadedRequest);
        assertEquals(authorizationRequest.getClientId(), loadedRequest.getClientId());
    }

    @Test
    void removeAuthorizationRequest_WithNoCookies_ReturnsNull() {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        OAuth2AuthorizationRequest removedRequest = repository.removeAuthorizationRequest(request, response);

        // Assert
        assertNull(removedRequest);
    }
}
