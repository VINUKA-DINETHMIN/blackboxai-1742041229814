package com.example.skillsharing.security;

import com.example.skillsharing.config.AppConfig;
import com.example.skillsharing.model.User;
import com.example.skillsharing.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TokenProviderTest {

    private TokenProvider tokenProvider;
    private AppConfig appConfig;
    private User testUser;
    private UserPrincipal userPrincipal;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        appConfig = new AppConfig();
        appConfig.setTokenSecret("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        appConfig.setTokenExpirationMsec(864000000L); // 10 days

        tokenProvider = new TokenProvider(appConfig);

        testUser = TestDataFactory.createUser(1L);
        userPrincipal = UserPrincipal.create(testUser);
        
        authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void createToken_WithValidAuthentication_ReturnsValidToken() {
        // Act
        String token = tokenProvider.createToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void getUserIdFromToken_WithValidToken_ReturnsCorrectUserId() {
        // Arrange
        String token = tokenProvider.createToken(authentication);

        // Act
        Long userId = tokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(testUser.getId(), userId);
    }

    @Test
    void validateToken_WithValidToken_ReturnsTrue() {
        // Arrange
        String token = tokenProvider.createToken(authentication);

        // Act & Assert
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateToken_WithInvalidToken_ReturnsFalse() {
        // Act & Assert
        assertFalse(tokenProvider.validateToken("invalid.token.string"));
    }

    @Test
    void validateToken_WithExpiredToken_ReturnsFalse() {
        // Arrange
        AppConfig shortExpirationConfig = new AppConfig();
        shortExpirationConfig.setTokenSecret(appConfig.getTokenSecret());
        shortExpirationConfig.setTokenExpirationMsec(1L); // 1 millisecond expiration

        TokenProvider shortExpirationTokenProvider = new TokenProvider(shortExpirationConfig);
        String token = shortExpirationTokenProvider.createToken(authentication);

        try {
            Thread.sleep(2); // Wait for token to expire
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert
        assertFalse(shortExpirationTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_WithNullToken_ReturnsFalse() {
        // Act & Assert
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    void validateToken_WithEmptyToken_ReturnsFalse() {
        // Act & Assert
        assertFalse(tokenProvider.validateToken(""));
    }

    @Test
    void createToken_PreservesUserAuthorities() {
        // Arrange
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        Authentication authWithAuthorities = new UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            Collections.singletonList(authority)
        );

        // Act
        String token = tokenProvider.createToken(authWithAuthorities);
        Authentication resultAuth = tokenProvider.getAuthentication(token);

        // Assert
        assertTrue(resultAuth.getAuthorities().contains(authority));
    }

    @Test
    void getAuthentication_ReturnsCorrectPrincipal() {
        // Arrange
        String token = tokenProvider.createToken(authentication);

        // Act
        Authentication resultAuth = tokenProvider.getAuthentication(token);

        // Assert
        assertTrue(resultAuth.getPrincipal() instanceof UserPrincipal);
        UserPrincipal resultPrincipal = (UserPrincipal) resultAuth.getPrincipal();
        assertEquals(userPrincipal.getId(), resultPrincipal.getId());
        assertEquals(userPrincipal.getEmail(), resultPrincipal.getEmail());
    }

    @Test
    void createToken_WithDifferentUsers_ProducesUniqueTokens() {
        // Arrange
        User anotherUser = TestDataFactory.createUser(2L);
        UserPrincipal anotherPrincipal = UserPrincipal.create(anotherUser);
        Authentication anotherAuth = new UsernamePasswordAuthenticationToken(
            anotherPrincipal,
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act
        String token1 = tokenProvider.createToken(authentication);
        String token2 = tokenProvider.createToken(anotherAuth);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void getUserIdFromToken_WithInvalidToken_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> 
            tokenProvider.getUserIdFromToken("invalid.token.string")
        );
    }
}
