package com.example.skillsharing.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException exception;

    @InjectMocks
    private OAuth2AuthenticationFailureHandler failureHandler;

    private static final String REDIRECT_URI = "http://localhost:3000/oauth2/redirect";

    @BeforeEach
    void setUp() {
        when(exception.getMessage()).thenReturn("Authentication failed");
    }

    @Test
    void onAuthenticationFailure_WithValidRedirectUri_RedirectsWithError() throws IOException, ServletException {
        // Arrange
        Cookie redirectUriCookie = new Cookie("redirect_uri", REDIRECT_URI);
        when(request.getCookies()).thenReturn(new Cookie[]{redirectUriCookie});

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        String expectedRedirectUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("error", "Authentication failed")
                .build().toUriString();
        verify(response).sendRedirect(expectedRedirectUrl);
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationFailure_WithNoCookies_HandlesGracefully() throws IOException, ServletException {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(anyString());
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationFailure_WithEmptyRedirectUri_HandlesGracefully() throws IOException, ServletException {
        // Arrange
        Cookie redirectUriCookie = new Cookie("redirect_uri", "");
        when(request.getCookies()).thenReturn(new Cookie[]{redirectUriCookie});

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(anyString());
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationFailure_WithMultipleCookies_FindsCorrectRedirectUri() throws IOException, ServletException {
        // Arrange
        Cookie redirectUriCookie = new Cookie("redirect_uri", REDIRECT_URI);
        Cookie otherCookie = new Cookie("other", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{otherCookie, redirectUriCookie});

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        String expectedRedirectUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("error", "Authentication failed")
                .build().toUriString();
        verify(response).sendRedirect(expectedRedirectUrl);
    }

    @Test
    void onAuthenticationFailure_WithNullException_HandlesGracefully() throws IOException, ServletException {
        // Arrange
        Cookie redirectUriCookie = new Cookie("redirect_uri", REDIRECT_URI);
        when(request.getCookies()).thenReturn(new Cookie[]{redirectUriCookie});

        // Act
        failureHandler.onAuthenticationFailure(request, response, null);

        // Assert
        String expectedRedirectUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("error", "Authentication failed")
                .build().toUriString();
        verify(response).sendRedirect(expectedRedirectUrl);
    }

    @Test
    void onAuthenticationFailure_WithIOException_HandlesGracefully() throws IOException, ServletException {
        // Arrange
        Cookie redirectUriCookie = new Cookie("redirect_uri", REDIRECT_URI);
        when(request.getCookies()).thenReturn(new Cookie[]{redirectUriCookie});
        doThrow(new IOException("Test exception")).when(response).sendRedirect(anyString());

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationFailure_WithInvalidRedirectUri_HandlesGracefully() throws IOException, ServletException {
        // Arrange
        Cookie redirectUriCookie = new Cookie("redirect_uri", "invalid:uri");
        when(request.getCookies()).thenReturn(new Cookie[]{redirectUriCookie});

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(anyString());
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationFailure_AlwaysCleansUpCookies() throws IOException, ServletException {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});
        doThrow(new RuntimeException("Unexpected error")).when(response).sendRedirect(anyString());

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationFailure_WithLongErrorMessage_TruncatesMessage() throws IOException, ServletException {
        // Arrange
        Cookie redirectUriCookie = new Cookie("redirect_uri", REDIRECT_URI);
        when(request.getCookies()).thenReturn(new Cookie[]{redirectUriCookie});
        String longErrorMessage = "A".repeat(1000);
        when(exception.getMessage()).thenReturn(longErrorMessage);

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(argThat(url -> url.length() < longErrorMessage.length()));
    }
}
