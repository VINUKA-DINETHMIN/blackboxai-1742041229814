package com.example.skillsharing.security.oauth2;

import com.example.skillsharing.exception.OAuth2AuthenticationProcessingException;
import com.example.skillsharing.model.User;
import com.example.skillsharing.repository.UserRepository;
import com.example.skillsharing.security.UserPrincipal;
import com.example.skillsharing.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2UserService delegateUserService;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private OAuth2UserRequest oAuth2UserRequest;
    private OAuth2User oAuth2User;
    private User testUser;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createUser(1L);
        attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("email", "test@example.com");
        attributes.put("name", "Test User");

        ClientRegistration clientRegistration = ClientRegistration
            .withRegistrationId("google")
            .clientId("client-id")
            .clientSecret("client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost/login/oauth2/code/google")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://www.googleapis.com/oauth2/v4/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .userNameAttributeName("sub")
            .scope("openid", "profile", "email")
            .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600)
        );

        oAuth2UserRequest = new OAuth2UserRequest(clientRegistration, accessToken);
        oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
    }

    @Test
    void loadUser_WithNewUser_CreatesAndReturnsUser() {
        // Arrange
        when(delegateUserService.loadUser(any())).thenReturn(oAuth2User);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof UserPrincipal);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loadUser_WithExistingUser_UpdatesAndReturnsUser() {
        // Arrange
        when(delegateUserService.loadUser(any())).thenReturn(oAuth2User);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof UserPrincipal);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loadUser_WithInvalidProvider_ThrowsException() {
        // Arrange
        when(delegateUserService.loadUser(any())).thenReturn(oAuth2User);
        oAuth2UserRequest = mock(OAuth2UserRequest.class);
        ClientRegistration invalidRegistration = mock(ClientRegistration.class);
        when(invalidRegistration.getRegistrationId()).thenReturn("invalid_provider");
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(invalidRegistration);

        // Act & Assert
        assertThrows(OAuth2AuthenticationProcessingException.class,
                () -> customOAuth2UserService.loadUser(oAuth2UserRequest));
    }

    @Test
    void loadUser_WithMissingEmail_ThrowsException() {
        // Arrange
        when(delegateUserService.loadUser(any())).thenReturn(oAuth2User);
        attributes.remove("email");

        // Act & Assert
        assertThrows(OAuth2AuthenticationProcessingException.class,
                () -> customOAuth2UserService.loadUser(oAuth2UserRequest));
    }

    @Test
    void loadUser_WithDelegateServiceFailure_PropagatesException() {
        // Arrange
        when(delegateUserService.loadUser(any()))
                .thenThrow(new RuntimeException("Delegate service failure"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> customOAuth2UserService.loadUser(oAuth2UserRequest));
    }

    @Test
    void loadUser_WithRepositoryFailure_PropagatesException() {
        // Arrange
        when(delegateUserService.loadUser(any())).thenReturn(oAuth2User);
        when(userRepository.findByEmail(anyString()))
                .thenThrow(new RuntimeException("Repository failure"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> customOAuth2UserService.loadUser(oAuth2UserRequest));
    }

    @Test
    void loadUser_PreservesUserAttributes() {
        // Arrange
        when(delegateUserService.loadUser(any())).thenReturn(oAuth2User);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        // Assert
        assertNotNull(result);
        assertEquals(attributes, result.getAttributes());
    }

    @Test
    void loadUser_UpdatesExistingUserProvider() {
        // Arrange
        testUser.setProvider("facebook");
        when(delegateUserService.loadUser(any())).thenReturn(oAuth2User);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        customOAuth2UserService.loadUser(oAuth2UserRequest);

        // Assert
        verify(userRepository).save(argThat(user -> 
            user.getProvider().equals("google")
        ));
    }
}
