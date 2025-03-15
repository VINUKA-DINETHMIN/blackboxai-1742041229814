package com.example.skillsharing.security;

import com.example.skillsharing.model.User;
import com.example.skillsharing.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    private User testUser;
    private UserPrincipal userPrincipal;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createUser(1L);
        userPrincipal = UserPrincipal.create(testUser);
        attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
    }

    @Test
    void create_FromUser_CreatesValidUserPrincipal() {
        // Act
        UserPrincipal principal = UserPrincipal.create(testUser);

        // Assert
        assertNotNull(principal);
        assertEquals(testUser.getId(), principal.getId());
        assertEquals(testUser.getEmail(), principal.getEmail());
        assertEquals(testUser.getPassword(), principal.getPassword());
        assertNotNull(principal.getAuthorities());
    }

    @Test
    void create_FromUserAndAttributes_CreatesValidUserPrincipal() {
        // Act
        OAuth2User principal = UserPrincipal.create(testUser, attributes);

        // Assert
        assertNotNull(principal);
        assertEquals(attributes, principal.getAttributes());
        assertTrue(principal instanceof UserPrincipal);
        assertEquals(testUser.getId(), ((UserPrincipal) principal).getId());
    }

    @Test
    void getAuthorities_ReturnsCorrectAuthorities() {
        // Act
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void getPassword_ReturnsCorrectPassword() {
        // Assert
        assertEquals(testUser.getPassword(), userPrincipal.getPassword());
    }

    @Test
    void getUsername_ReturnsEmail() {
        // Assert
        assertEquals(testUser.getEmail(), userPrincipal.getUsername());
    }

    @Test
    void isAccountNonExpired_ReturnsTrue() {
        // Assert
        assertTrue(userPrincipal.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_ReturnsTrue() {
        // Assert
        assertTrue(userPrincipal.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_ReturnsTrue() {
        // Assert
        assertTrue(userPrincipal.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_ReturnsTrue() {
        // Assert
        assertTrue(userPrincipal.isEnabled());
    }

    @Test
    void getAttributes_WithNoAttributes_ReturnsEmptyMap() {
        // Assert
        assertTrue(userPrincipal.getAttributes().isEmpty());
    }

    @Test
    void getAttributes_WithAttributes_ReturnsCorrectMap() {
        // Arrange
        UserPrincipal principalWithAttributes = UserPrincipal.create(testUser, attributes);

        // Assert
        assertEquals(attributes, principalWithAttributes.getAttributes());
    }

    @Test
    void getName_ReturnsId() {
        // Assert
        assertEquals(testUser.getId().toString(), userPrincipal.getName());
    }

    @Test
    void equals_WithSameUser_ReturnsTrue() {
        // Arrange
        UserPrincipal samePrincipal = UserPrincipal.create(testUser);

        // Assert
        assertEquals(userPrincipal, samePrincipal);
        assertEquals(userPrincipal.hashCode(), samePrincipal.hashCode());
    }

    @Test
    void equals_WithDifferentUser_ReturnsFalse() {
        // Arrange
        User differentUser = TestDataFactory.createUser(2L);
        UserPrincipal differentPrincipal = UserPrincipal.create(differentUser);

        // Assert
        assertNotEquals(userPrincipal, differentPrincipal);
        assertNotEquals(userPrincipal.hashCode(), differentPrincipal.hashCode());
    }

    @Test
    void equals_WithNull_ReturnsFalse() {
        // Assert
        assertNotEquals(userPrincipal, null);
    }

    @Test
    void equals_WithDifferentClass_ReturnsFalse() {
        // Assert
        assertNotEquals(userPrincipal, new Object());
    }

    @Test
    void builder_CreatesValidUserPrincipal() {
        // Act
        UserPrincipal principal = UserPrincipal.builder()
                .id(testUser.getId())
                .email(testUser.getEmail())
                .password(testUser.getPassword())
                .authorities(userPrincipal.getAuthorities())
                .attributes(attributes)
                .build();

        // Assert
        assertNotNull(principal);
        assertEquals(testUser.getId(), principal.getId());
        assertEquals(testUser.getEmail(), principal.getEmail());
        assertEquals(testUser.getPassword(), principal.getPassword());
        assertEquals(attributes, principal.getAttributes());
    }

    @Test
    void create_WithNullUser_ThrowsException() {
        // Assert
        assertThrows(NullPointerException.class, () -> UserPrincipal.create(null));
    }

    @Test
    void create_WithNullAttributes_CreatesValidUserPrincipal() {
        // Act
        OAuth2User principal = UserPrincipal.create(testUser, null);

        // Assert
        assertNotNull(principal);
        assertTrue(principal.getAttributes().isEmpty());
    }
}
