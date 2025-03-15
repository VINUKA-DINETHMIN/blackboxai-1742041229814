package com.example.skillsharing.config;

import com.example.skillsharing.security.CustomUserDetailsService;
import com.example.skillsharing.security.TokenAuthenticationFilter;
import com.example.skillsharing.security.TokenProvider;
import com.example.skillsharing.security.oauth2.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Mock
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private HttpSecurity httpSecurity;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(
            customUserDetailsService,
            customOAuth2UserService,
            oAuth2AuthenticationSuccessHandler,
            oAuth2AuthenticationFailureHandler,
            tokenProvider
        );
    }

    @Test
    void tokenAuthenticationFilter_ReturnsValidFilter() {
        // Act
        TokenAuthenticationFilter filter = securityConfig.tokenAuthenticationFilter();

        // Assert
        assertNotNull(filter);
    }

    @Test
    void passwordEncoder_ReturnsBCryptPasswordEncoder() {
        // Act
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // Assert
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void authenticationManager_ReturnsValidManager() throws Exception {
        // Arrange
        AuthenticationManager expectedManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(expectedManager);

        // Act
        AuthenticationManager manager = securityConfig.authenticationManager(authenticationConfiguration);

        // Assert
        assertNotNull(manager);
        assertEquals(expectedManager, manager);
    }

    @Test
    void cookieAuthorizationRequestRepository_ReturnsValidRepository() {
        // Act
        HttpCookieOAuth2AuthorizationRequestRepository repository = 
            securityConfig.cookieAuthorizationRequestRepository();

        // Assert
        assertNotNull(repository);
    }

    @Test
    void passwordEncoder_GeneratesDifferentHashesForSamePassword() {
        // Arrange
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String password = "testPassword";

        // Act
        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);

        // Assert
        assertNotEquals(hash1, hash2);
        assertTrue(encoder.matches(password, hash1));
        assertTrue(encoder.matches(password, hash2));
    }

    @Test
    void passwordEncoder_ValidatesCorrectPassword() {
        // Arrange
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String password = "testPassword";
        String hash = encoder.encode(password);

        // Assert
        assertTrue(encoder.matches(password, hash));
    }

    @Test
    void passwordEncoder_RejectsIncorrectPassword() {
        // Arrange
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String password = "testPassword";
        String wrongPassword = "wrongPassword";
        String hash = encoder.encode(password);

        // Assert
        assertFalse(encoder.matches(wrongPassword, hash));
    }

    @Test
    void tokenAuthenticationFilter_CreatesNewInstanceEachTime() {
        // Act
        TokenAuthenticationFilter filter1 = securityConfig.tokenAuthenticationFilter();
        TokenAuthenticationFilter filter2 = securityConfig.tokenAuthenticationFilter();

        // Assert
        assertNotSame(filter1, filter2);
    }

    @Test
    void cookieAuthorizationRequestRepository_CreatesNewInstanceEachTime() {
        // Act
        HttpCookieOAuth2AuthorizationRequestRepository repo1 = 
            securityConfig.cookieAuthorizationRequestRepository();
        HttpCookieOAuth2AuthorizationRequestRepository repo2 = 
            securityConfig.cookieAuthorizationRequestRepository();

        // Assert
        assertNotSame(repo1, repo2);
    }

    @Test
    void securityFilterChain_ConfiguresSecurityFilters() throws Exception {
        // Arrange
        when(httpSecurity.csrf()).thenReturn(new AbstractHttpConfigurer<>() {});
        when(httpSecurity.cors()).thenReturn(new AbstractHttpConfigurer<>() {});
        when(httpSecurity.sessionManagement()).thenReturn(new AbstractHttpConfigurer<>() {});
        when(httpSecurity.authorizeHttpRequests()).thenReturn(new AbstractHttpConfigurer<>() {});
        when(httpSecurity.oauth2Login()).thenReturn(new AbstractHttpConfigurer<>() {});
        when(httpSecurity.build()).thenReturn(mock(SecurityFilterChain.class));

        // Act
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(httpSecurity);

        // Assert
        assertNotNull(filterChain);
        verify(httpSecurity).csrf();
        verify(httpSecurity).cors();
        verify(httpSecurity).sessionManagement();
        verify(httpSecurity).authorizeHttpRequests();
        verify(httpSecurity).oauth2Login();
    }
}
