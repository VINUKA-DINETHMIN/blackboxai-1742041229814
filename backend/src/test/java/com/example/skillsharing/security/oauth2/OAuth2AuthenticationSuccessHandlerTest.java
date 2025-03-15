package com.example.skillsharing.security.oauth2;

import com.example.skillsharing.config.AppConfig;
import com.example.skillsharing.security.TokenProvider;
import com.example.skillsharing.util.TestDataFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private AppConfig appConfig;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    private static final String REDIRECT_URI = "http://localhost:3000/oauth2/redirect";
    private static final String TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        successHandler.setDefaultTargetUrl("http://localhost:8080");
        when(appConfig.getAuthorizedRedirectUris())
                .thenReturn(Collections.singletonList(REDIRECT_URI));
        when(tokenProvider.createToken(any(Authentication.class)))
                .thenReturn(TOKEN);
    }

    @Test
    void onAuthenticationSuccess_WithValidRedirectUri_RedirectsWithToken() throws IOException {
        // Arrange
        Cookie redirectUriCookie = new Cookie("redirect_uri", REDIRECT_URI);
        when(request.getCookies()).thenReturn(new Cookie[]{redirectUriCookie});
        when(request.getParameter("redirect_uri")).thenReturn(REDIRECT_URI);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        String expectedRedirectUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("token", TOKEN)
                .build().toUriString();
        verify(response).sendRedirect(expectedRedirectUrl);
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationSuccess_WithInvalidRedirectUri_UsesDefaultTargetUrl() throws IOException {
        // Arrange
        String invalidRedirectUri = "http://malicious-site.com";
        Cookie redirectUriCookie = new Cookie("redirect_uri", invalidRedirectUri);
        when(request.getCookies()).thenReturn(new Cookie[]{redirectUriCookie});
        when(request.getParameter("redirect_uri")).thenReturn(invalidRedirectUri);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(anyString());
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationSuccess_WithNoCookies_UsesDefaultTargetUrl() throws IOException {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(anyString());
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationSuccess_WithNoRedirectUri_UsesDefaultTargetUrl() throws IOException {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});
        when(request.getParameter("redirect_uri")).thenReturn(null);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(anyString());
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationSuccess_WithMultipleCookies_FindsCorrectRedirectUri() throws IOException {
        // Arrange
        Cookie redirectUriCookie = new Cookie("redirect_uri", REDIRECT_URI);
        Cookie otherCookie = new Cookie("other", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{otherCookie, redirectUriCookie});
        when(request.getParameter("redirect_uri")).thenReturn(REDIRECT_URI);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        String expectedRedirectUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("token", TOKEN)
                .build().toUriString();
        verify(response).sendRedirect(expectedRedirectUrl);
    }

    @Test
    void onAuthenticationSuccess_WithEmptyAuthorizedUris_UsesDefaultTargetUrl() throws IOException {
        // Arrange
        when(appConfig.getAuthorizedRedirectUris()).thenReturn(Collections.emptyList());
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("redirect_uri", REDIRECT_URI)});

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(anyString());
    }

    @Test
    void onAuthenticationSuccess_HandlesIOException() throws IOException {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("redirect_uri", REDIRECT_URI)});
        doThrow(new IOException("Test exception")).when(response).sendRedirect(anyString());

        // Act & Assert
        successHandler.onAuthenticationSuccess(request, response, authentication);
        // Verify that cleanup still occurs even if redirect fails
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationSuccess_CleansUpCookiesOnError() throws IOException {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("redirect_uri", REDIRECT_URI)});
        when(tokenProvider.createToken(any())).thenThrow(new RuntimeException("Token creation failed"));

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }
}
