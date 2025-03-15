package com.example.skillsharing.config;

import com.example.skillsharing.security.TokenProvider;
import com.example.skillsharing.security.UserPrincipal;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public TokenProvider testTokenProvider() {
        TokenProvider mockTokenProvider = mock(TokenProvider.class);
        when(mockTokenProvider.createToken(any(Authentication.class)))
                .thenReturn("test-jwt-token");
        when(mockTokenProvider.validateToken(any(String.class)))
                .thenReturn(true);
        when(mockTokenProvider.getUserIdFromToken(any(String.class)))
                .thenReturn(1L);
        return mockTokenProvider;
    }

    @Bean
    @Primary
    public ClientRegistrationRepository testClientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository();
    }

    @Bean
    @Primary
    public RestTemplate testRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SecurityContext testSecurityContext() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        return securityContext;
    }

    @Bean
    public UserPrincipal testUserPrincipal() {
        return UserPrincipal.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }
}
